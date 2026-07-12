package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 공식 챌린지 예약 노출(visible_from) 계약: 클라 조회 쿼리(랜덤/목록/검색)는 visible_from 이 미래인 챌린지를 제외하고, null/과거는 포함한다.
 * 어드민 조회는 예약 여부와 무관하게 전부 노출한다. 실 Postgres 로 SQL(널 처리·timestamp 비교)을 고정한다.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OfficialChallengeVisibilityPostgresTest {

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
    r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    r.add("spring.jpa.properties.hibernate.default_schema", () -> "odos_dev");
    r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    r.add("spring.flyway.enabled", () -> "true");
    r.add("spring.flyway.schemas", () -> "odos_dev");
    r.add("spring.flyway.default-schema", () -> "odos_dev");
    r.add("spring.flyway.create-schemas", () -> "true");
  }

  @Autowired ChallengeRepository challengeRepository;
  @Autowired MemberRepository memberRepository;

  private final LocalDate today = LocalDate.now();
  private final LocalDateTime now = LocalDateTime.now();

  private Challenge save(String title, LocalDateTime visibleFrom) {
    Member host = memberRepository.save(Member.builder().email(title + "@t.com").build());
    return challengeRepository.save(
        Challenge.builder()
            .title(title)
            .category(Category.DEV)
            .startDate(today.minusDays(1))
            .endDate(today.plusDays(30))
            .challengeType(ChallengeType.OFFICIAL)
            .participationType(ParticipationType.GROUP)
            .visibleFrom(visibleFrom)
            .hostMember(host)
            .build());
  }

  private List<Long> searchPageIds() {
    return challengeRepository
        .searchPage(
            null,
            "",
            ChallengeType.PRIVATE.name(),
            null,
            true,
            List.of(),
            true,
            List.of(),
            today,
            now,
            PageRequest.of(0, 50))
        .stream()
        .map(Challenge::getId)
        .toList();
  }

  private List<Long> findByFiltersIds() {
    return challengeRepository
        .findByFilters(
            null,
            true,
            List.of(),
            ChallengeType.PRIVATE.name(),
            null,
            true,
            List.of(),
            today,
            now,
            PageRequest.of(0, 50))
        .stream()
        .map(Challenge::getId)
        .toList();
  }

  @Test
  void clientQueries_excludeFutureVisibleFrom_includeNullAndPast() {
    Challenge immediate = save("immediate", null); // 즉시 노출
    Challenge past = save("past", now.minusHours(1)); // 노출 시작됨
    Challenge scheduled = save("scheduled", now.plusDays(1)); // 예약(노출 전)

    // 랜덤/추천
    List<Long> randomIds =
        challengeRepository.findRandomActiveChallenges(today, now, PageRequest.of(0, 50)).stream()
            .map(Challenge::getId)
            .toList();
    assertThat(randomIds).contains(immediate.getId(), past.getId());
    assertThat(randomIds).doesNotContain(scheduled.getId());

    // 커서 목록 + 오프셋 목록 + 검색
    assertThat(searchPageIds()).contains(immediate.getId(), past.getId());
    assertThat(searchPageIds()).doesNotContain(scheduled.getId());
    assertThat(findByFiltersIds()).contains(immediate.getId(), past.getId());
    assertThat(findByFiltersIds()).doesNotContain(scheduled.getId());
  }

  @Test
  void adminQuery_includesScheduledChallenge() {
    Challenge scheduled = save("scheduled-admin", now.plusDays(1));

    Page<Challenge> adminPage =
        challengeRepository.findAdminChallengesOrderByLatest(
            null, null, null, today, PageRequest.of(0, 50));

    assertThat(adminPage.getContent()).extracting(Challenge::getId).contains(scheduled.getId());
  }

  @Test
  void visibleFrom_roundTripsThroughPostgres() {
    Challenge saved = save("roundtrip", now.plusDays(2));
    Challenge reloaded = challengeRepository.findById(saved.getId()).orElseThrow();
    assertThat(reloaded.getVisibleFrom()).isNotNull();
    assertThat(reloaded.getVisibleFrom()).isAfter(now);
  }
}
