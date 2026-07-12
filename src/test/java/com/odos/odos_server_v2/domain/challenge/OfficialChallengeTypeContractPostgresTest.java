package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.challenge.dto.OfficialChallengeRequest;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.FixedChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.challenge.service.OfficialChallengeService;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
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
 * 어드민 계약 락(계약 대조 #1): 어드민은 공식 챌린지 생성 시 {@code challengeType} 을 바디에 보내지 않고
 * 엔드포인트(/official-challenges)로 OFFICIAL 을 추론한다. 서버가 실제로 OFFICIAL 로 저장하는지(그리고 요청 DTO 가 challengeType
 * 필드를 갖지 않는지)를 실 Postgres 로 고정한다.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OfficialChallengeTypeContractPostgresTest {

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
  @Autowired ChallengeGoalRepository challengeGoalRepository;
  @Autowired FixedChallengeGoalRepository fixedChallengeGoalRepository;
  @Autowired ParticipantRepository participantRepository;
  @Autowired DiaryRepository diaryRepository;
  @Autowired MemberRepository memberRepository;

  private OfficialChallengeService service() {
    // createOfficialChallenge 는 persist 이후 반환값 생성에만 ChallengeService 를 쓰므로 mock 으로 충분.
    return new OfficialChallengeService(
        challengeRepository,
        challengeGoalRepository,
        fixedChallengeGoalRepository,
        participantRepository,
        diaryRepository,
        memberRepository,
        mock(ChallengeService.class));
  }

  @Test
  void createOfficialChallenge_withoutChallengeTypeInBody_persistsAsOfficialGroup() {
    Member admin =
        memberRepository.save(Member.builder().email("admin@t.com").role(MemberRole.ADMIN).build());

    OfficialChallengeRequest request =
        OfficialChallengeRequest.builder()
            .title("공식 챌린지")
            .category(com.odos.odos_server_v2.domain.shared.Enum.Category.DEV)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .goalType(GoalType.FIXED)
            .goals(List.of("알고리즘 1문제 풀기"))
            .build();

    service().createOfficialChallenge(request, admin.getId());

    List<Challenge> all = challengeRepository.findAll();
    assertThat(all).hasSize(1);
    Challenge saved = all.get(0);
    assertThat(saved.getChallengeType()).isEqualTo(ChallengeType.OFFICIAL);
    assertThat(saved.getParticipationType()).isEqualTo(ParticipationType.GROUP);
  }

  @Test
  void officialChallengeRequest_hasNoChallengeTypeField() throws Exception {
    // 계약: 요청 바디에 challengeType 을 담을 수 없음(엔드포인트로만 OFFICIAL 결정). 필드가 생기면 계약이 깨진 것.
    for (Field f : OfficialChallengeRequest.class.getDeclaredFields()) {
      assertThat(f.getName()).isNotEqualToIgnoringCase("challengeType");
    }
  }
}
