package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * GET /challenges/my/today 를 뒷받침하는 배치 쿼리(findInProgressWithGoals +
 * findChallengeIdsWithDiaryOnDate)를 실 Postgres + 실 Flyway 로 검증. (도커 필요)
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MyTodayChallengePostgresTest {

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

  @Autowired MemberRepository memberRepository;
  @Autowired ChallengeRepository challengeRepository;
  @Autowired ParticipantRepository participantRepository;
  @Autowired ChallengeGoalRepository challengeGoalRepository;
  @Autowired DiaryRepository diaryRepository;
  @Autowired TestEntityManager em;

  private final LocalDate today = LocalDate.of(2026, 7, 12);

  private Challenge saveChallenge(Member host, LocalDate start, LocalDate end) {
    return challengeRepository.save(
        Challenge.builder()
            .title("c")
            .hostMember(host)
            .startDate(start)
            .endDate(end)
            .challengeType(ChallengeType.PUBLIC)
            .goalType(GoalType.FLEXIBLE)
            .participationType(ParticipationType.GROUP)
            .build());
  }

  private Participant join(Member m, Challenge c, ParticipantStatus status, String... goals) {
    Participant p =
        participantRepository.save(
            Participant.builder().member(m).challenge(c).status(status).build());
    for (String g : goals) {
      challengeGoalRepository.save(ChallengeGoal.builder().content(g).participant(p).build());
    }
    return p;
  }

  @Test
  void findInProgressWithGoals_returnsOnlyInProgressActiveWithGoalsFetched() {
    Member me = memberRepository.save(Member.builder().email("me@t.com").build());
    Member other = memberRepository.save(Member.builder().email("o@t.com").build());

    Challenge inProgress = saveChallenge(me, today.minusDays(1), today.plusDays(3));
    join(me, inProgress, ParticipantStatus.PARTICIPANT, "goal-a", "goal-b");

    Challenge ended = saveChallenge(me, today.minusDays(9), today.minusDays(1));
    join(me, ended, ParticipantStatus.PARTICIPANT, "x"); // 종료 → 제외

    Challenge upcoming = saveChallenge(me, today.plusDays(1), today.plusDays(9));
    join(me, upcoming, ParticipantStatus.PARTICIPANT, "y"); // 예정 → 제외

    Challenge pendingChallenge = saveChallenge(me, today.minusDays(1), today.plusDays(3));
    join(me, pendingChallenge, ParticipantStatus.PENDING, "z"); // 신청중 → 제외

    Challenge deleted = saveChallenge(me, today.minusDays(1), today.plusDays(3));
    deleted.softDelete();
    challengeRepository.save(deleted);
    join(me, deleted, ParticipantStatus.PARTICIPANT, "w"); // 삭제 → 제외

    Challenge othersChallenge = saveChallenge(other, today.minusDays(1), today.plusDays(3));
    join(other, othersChallenge, ParticipantStatus.HOST, "not-mine"); // 타인 → 제외

    // fetch join 이 fresh 로딩되도록 영속성 컨텍스트를 비운다(같은 트랜잭션 내 managed 엔티티의
    // 이미 초기화된 빈 컬렉션이 fetch 결과로 덮어써지지 않는 @DataJpaTest 특성 회피).
    em.flush();
    em.clear();

    List<Participant> result =
        participantRepository.findInProgressWithGoals(
            me.getId(), List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT), today);

    assertThat(result).hasSize(1);
    Participant p = result.get(0);
    assertThat(p.getChallenge().getId()).isEqualTo(inProgress.getId());
    assertThat(p.getChallengeGoals())
        .extracting(ChallengeGoal::getContent)
        .containsExactlyInAnyOrder("goal-a", "goal-b");
  }

  @Test
  void findChallengeIdsWithDiaryOnDate_returnsOnlyChallengesWrittenTodayByMember() {
    Member me = memberRepository.save(Member.builder().email("me2@t.com").build());
    Member other = memberRepository.save(Member.builder().email("o2@t.com").build());

    Challenge written = saveChallenge(me, today.minusDays(1), today.plusDays(3));
    Challenge notWritten = saveChallenge(me, today.minusDays(1), today.plusDays(3));

    // 오늘 작성(대상)
    diaryRepository.save(diary(me, written, today, false));
    // 어제 작성 → 제외
    diaryRepository.save(diary(me, notWritten, today.minusDays(1), false));
    // 오늘 작성했으나 삭제 → 제외
    diaryRepository.save(diary(me, notWritten, today, true));
    // 타인이 오늘 작성 → 제외
    diaryRepository.save(diary(other, notWritten, today, false));

    List<Long> result =
        diaryRepository.findChallengeIdsWithDiaryOnDate(
            me.getId(), Set.of(written.getId(), notWritten.getId()), today);

    assertThat(result).containsExactly(written.getId());
  }

  private Diary diary(Member m, Challenge c, LocalDate date, boolean deleted) {
    return Diary.builder()
        .member(m)
        .challenge(c)
        .completedDate(date)
        .isPublic(true)
        .isDeleted(deleted)
        .build();
  }
}
