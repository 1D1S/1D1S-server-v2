package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeStatisticsResponse;
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
import org.junit.jupiter.api.BeforeEach;
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
 * 챌린지 통계 500 재현/회귀: 무기한(endDate==null) 챌린지에서 참여율 계산이 NPE 나지 않아야 한다. (실 Postgres + 실 Flyway, 실
 * ChallengeService)
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChallengeStatisticsServicePostgresTest {

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

  private ChallengeService service;

  @BeforeEach
  void setUp() {
    service =
        new ChallengeService(
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

  /** endDate==null(무기한) + 시작됨 + 참여자 1명: 기존엔 today.isBefore(null) 로 NPE(500). 이제 정상 응답. */
  @Test
  void statistics_onUnlimitedStartedChallenge_doesNotThrow() {
    LocalDate today = LocalDate.now();
    Member host = memberRepository.save(Member.builder().email("host@t.com").build());
    Challenge ch =
        challengeRepository.save(
            Challenge.builder()
                .title("무기한 챌린지")
                .hostMember(host)
                .startDate(today.minusDays(3))
                .endDate(null) // 무기한
                .challengeType(ChallengeType.PUBLIC)
                .goalType(GoalType.FIXED)
                .participationType(ParticipationType.GROUP)
                .maxParticipantsCnt(10L)
                .build());
    participantRepository.save(
        Participant.builder().member(host).challenge(ch).status(ParticipantStatus.HOST).build());

    ChallengeStatisticsResponse[] holder = new ChallengeStatisticsResponse[1];
    assertThatCode(() -> holder[0] = service.getChallengeStatistics(ch.getId(), host.getId()))
        .doesNotThrowAnyException();

    ChallengeStatisticsResponse res = holder[0];
    assertThat(res.participationRate()).isGreaterThanOrEqualTo(0.0);
    // 무기한 → startDate ~ 오늘: 4일(오늘-3 ~ 오늘)
    assertThat(res.diaryTrend()).hasSize(4);
    assertThat(res.diaryTrend().get(res.diaryTrend().size() - 1).date()).isEqualTo(today);
  }

  /** 시작 전(startDate 미래) 챌린지: participationRate -1, diaryTrend 빈 배열, NPE 없음. */
  @Test
  void statistics_onUpcomingUnlimitedChallenge_doesNotThrow() {
    LocalDate today = LocalDate.now();
    Member host = memberRepository.save(Member.builder().email("host2@t.com").build());
    Challenge ch =
        challengeRepository.save(
            Challenge.builder()
                .title("시작 전 무기한")
                .hostMember(host)
                .startDate(today.plusDays(2))
                .endDate(null)
                .challengeType(ChallengeType.PUBLIC)
                .goalType(GoalType.FIXED)
                .participationType(ParticipationType.GROUP)
                .maxParticipantsCnt(10L)
                .build());
    participantRepository.save(
        Participant.builder().member(host).challenge(ch).status(ParticipantStatus.HOST).build());

    assertThatCode(() -> service.getChallengeStatistics(ch.getId(), host.getId()))
        .doesNotThrowAnyException();
  }
}
