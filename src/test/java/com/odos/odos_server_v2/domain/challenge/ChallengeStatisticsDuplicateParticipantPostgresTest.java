package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengePokeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.FixedChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeRankingService;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통계 500 회귀: PRIVATE 챌린지는 권한 확인차 getMemberStatus 로 조회자의 참여자 행을 단건 조회한다. participant 에 (member_id,
 * challenge_id) 유니크 제약이 없어 조회자에게 중복 참여자 행이 있으면 이전엔 NonUniqueResultException(500) 이 났다. 이제 First 조회로
 * 예외 없이 응답해야 한다. (실 Postgres + 실 Flyway)
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChallengeStatisticsDuplicateParticipantPostgresTest {

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

  @Autowired ParticipantRepository participantRepository;
  @Autowired DiaryGoalRepository diaryGoalRepository;
  @Autowired DiaryRepository diaryRepository;
  @Autowired ChallengeRepository challengeRepository;
  @Autowired MemberRepository memberRepository;

  private ChallengeService service() {
    return new ChallengeService(
        participantRepository,
        mock(ChallengeLikeRepository.class),
        mock(ChallengeGoalRepository.class),
        mock(FixedChallengeGoalRepository.class),
        mock(ChallengePokeRepository.class),
        diaryGoalRepository,
        diaryRepository,
        mock(ImageService.class),
        challengeRepository,
        memberRepository,
        mock(CursorService.class),
        mock(NotificationService.class),
        mock(ChallengeRankingService.class),
        mock(PasswordEncoder.class));
  }

  @Test
  void statistics_privateChallenge_callerHasDuplicateParticipantRows_doesNotThrow() {
    LocalDate today = LocalDate.now();
    Member host = memberRepository.save(Member.builder().email("dup@t.com").build());

    Challenge ch =
        challengeRepository.save(
            Challenge.builder()
                .title("PRIVATE 중복참여자")
                .hostMember(host)
                .startDate(today.minusDays(3))
                .endDate(today.plusDays(3))
                .challengeType(ChallengeType.PRIVATE)
                .goalType(GoalType.FIXED)
                .participationType(ParticipationType.GROUP)
                .maxParticipantsCnt(10L)
                .build());
    // 같은 member+challenge 참여자 행 2개(유니크 제약 없음 → 더티 데이터로 존재 가능)
    participantRepository.save(
        Participant.builder().member(host).challenge(ch).status(ParticipantStatus.HOST).build());
    participantRepository.save(
        Participant.builder().member(host).challenge(ch).status(ParticipantStatus.HOST).build());

    assertThatCode(() -> service().getChallengeStatistics(ch.getId(), host.getId()))
        .doesNotThrowAnyException();
  }
}
