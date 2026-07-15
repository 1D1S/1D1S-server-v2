package com.odos.odos_server_v2.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.member.entity.Interest;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.InterestRepository;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 카테고리 개편 마이그레이션(V38) 계약: 제거된 구값(MUSIC/LEISURE/STUDY/ECONOMY)이 실제 Postgres 에서 신값으로 재매핑되고, 재매핑 후 남은
 * 구값이 0건이며, 재매핑된 행을 JPA(신 enum)로 읽을 때 예외가 없음을 고정한다. Flyway 가 부팅 시 V1~V38 을 정상 적용한다는 것 자체가 V38 SQL 이
 * 유효한 Postgres 이고 ddl-auto validate 로 앱이 기동됨을 보장한다.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRemapMigrationPostgresTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("odos")
          .withUsername("odos")
          .withPassword("odos");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    r.add("spring.datasource.username", POSTGRES::getUsername);
    r.add("spring.datasource.password", POSTGRES::getPassword);
    r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    // 스키마가 마이그레이션과 일치하는지 앱 기동 시점에 검증한다.
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    r.add("spring.jpa.properties.hibernate.default_schema", () -> "odos_dev");
    r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.schemas", () -> "odos_dev");
    r.add("spring.flyway.default-schema", () -> "odos_dev");
    r.add("spring.flyway.create-schemas", () -> "true");
  }

  @Autowired ChallengeRepository challengeRepository;
  @Autowired MemberRepository memberRepository;
  @Autowired InterestRepository interestRepository;
  @Autowired EntityManager em;

  private final LocalDate today = LocalDate.now();

  private Challenge saveChallenge(String title) {
    Member host = memberRepository.save(Member.builder().email(title + "@t.com").build());
    return challengeRepository.save(
        Challenge.builder()
            .title(title)
            .category(Category.DEV)
            .startDate(today.minusDays(1))
            .endDate(today.plusDays(30))
            .challengeType(ChallengeType.OFFICIAL)
            .participationType(ParticipationType.GROUP)
            .hostMember(host)
            .build());
  }

  private Long saveInterest(String email) {
    Member m = memberRepository.save(Member.builder().email(email).build());
    return interestRepository.save(new Interest(m, Category.DEV)).getId();
  }

  private void setRawCategory(String table, Long id, String oldValue) {
    em.createNativeQuery("UPDATE odos_dev." + table + " SET category = ?1 WHERE id = ?2")
        .setParameter(1, oldValue)
        .setParameter(2, id)
        .executeUpdate();
  }

  /** V38 파일의 UPDATE 문을 그대로 실행해 실제 마이그레이션 로직을 검증한다(테스트 내 SQL 중복 방지). */
  private void runV38Updates() throws IOException {
    String sql;
    try (InputStream in =
        getClass().getResourceAsStream("/db/migration/V38__remap_categories.sql")) {
      sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    // 라인 주석(--)을 제거한 뒤, DO $$ ... $$ 검증 블록은 빼고 단순 UPDATE 문만 실행한다.
    StringBuilder cleaned = new StringBuilder();
    for (String line : sql.split("\n")) {
      if (!line.stripLeading().startsWith("--")) {
        cleaned.append(line).append('\n');
      }
    }
    for (String raw : cleaned.toString().split(";")) {
      String stmt = raw.strip();
      if (stmt.regionMatches(true, 0, "UPDATE", 0, 6)) {
        em.createNativeQuery(stmt).executeUpdate();
      }
    }
  }

  @Test
  void remapsRemovedCategories_toNewValues_andReadsBackThroughJpa() throws IOException {
    em.createNativeQuery("SET search_path TO odos_dev").executeUpdate();

    // 구값을 심는다(컬럼이 VARCHAR 라 enum 에 없는 값도 저장됨).
    Long music = saveChallenge("c-music").getId();
    Long leisure = saveChallenge("c-leisure").getId();
    Long study = saveChallenge("c-study").getId();
    Long economy = saveChallenge("c-economy").getId();
    em.flush();
    setRawCategory("challenge", music, "MUSIC");
    setRawCategory("challenge", leisure, "LEISURE");
    setRawCategory("challenge", study, "STUDY");
    setRawCategory("challenge", economy, "ECONOMY");

    Long iMusic = saveInterest("i-music@t.com");
    Long iEconomy = saveInterest("i-economy@t.com");
    em.flush();
    setRawCategory("interest", iMusic, "MUSIC");
    setRawCategory("interest", iEconomy, "ECONOMY");

    // 마이그레이션 재매핑 실행.
    runV38Updates();
    em.clear();

    // JPA(신 enum)로 재조회 시 예외 없이 신값으로 읽힌다.
    assertThat(challengeRepository.findById(music).orElseThrow().getCategory())
        .isEqualTo(Category.HOBBY);
    assertThat(challengeRepository.findById(leisure).orElseThrow().getCategory())
        .isEqualTo(Category.HOBBY);
    assertThat(challengeRepository.findById(study).orElseThrow().getCategory())
        .isEqualTo(Category.SELF_DEV);
    assertThat(challengeRepository.findById(economy).orElseThrow().getCategory())
        .isEqualTo(Category.ETC);
    assertThat(interestRepository.findById(iMusic).orElseThrow().getCategory())
        .isEqualTo(Category.HOBBY);
    assertThat(interestRepository.findById(iEconomy).orElseThrow().getCategory())
        .isEqualTo(Category.ETC);

    // 재매핑 후 남은 구값이 0건.
    Number leftover =
        (Number)
            em.createNativeQuery(
                    "SELECT count(*) FROM (SELECT category FROM odos_dev.challenge UNION ALL SELECT"
                        + " category FROM odos_dev.interest) c WHERE category IN ('MUSIC',"
                        + " 'LEISURE', 'STUDY', 'ECONOMY')")
                .getSingleResult();
    assertThat(leftover.longValue()).isZero();
  }
}
