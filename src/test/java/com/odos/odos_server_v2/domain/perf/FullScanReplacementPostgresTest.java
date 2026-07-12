package com.odos.odos_server_v2.domain.perf;

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
import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.diary.repository.DiaryLikeRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * findAll() 전량 로드를 대체한 DB 집계/랜덤 쿼리를 실 Postgres + 실 Flyway 로 검증. (도커 필요)
 *
 * <p>검증 대상: findRandomActiveChallenges, findRandomPublicDiaries, countTodayInProgressGoals,
 * countByDiaryId.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FullScanReplacementPostgresTest {

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
  @Autowired DiaryLikeRepository diaryLikeRepository;

  private final LocalDate today = LocalDate.of(2026, 7, 12);

  private Challenge saveChallenge(
      Member host, ChallengeType type, LocalDate start, LocalDate end, boolean deleted) {
    Challenge c =
        challengeRepository.save(
            Challenge.builder()
                .title("c")
                .hostMember(host)
                .startDate(start)
                .endDate(end)
                .challengeType(type)
                .goalType(GoalType.FIXED)
                .participationType(ParticipationType.GROUP)
                .build());
    if (deleted) {
      c.softDelete();
      challengeRepository.save(c);
    }
    return c;
  }

  @Test
  void findRandomActiveChallenges_excludesPrivateDeletedEnded_includesUnlimited() {
    Member host = memberRepository.save(Member.builder().email("h@t.com").build());
    Challenge active =
        saveChallenge(host, ChallengeType.PUBLIC, today.minusDays(1), today.plusDays(3), false);
    Challenge unlimited =
        saveChallenge(
            host, ChallengeType.PUBLIC, today.minusDays(1), LocalDate.of(9999, 12, 31), false);
    saveChallenge(host, ChallengeType.PRIVATE, today.minusDays(1), today.plusDays(3), false); // 제외
    saveChallenge(host, ChallengeType.PUBLIC, today.minusDays(9), today.minusDays(1), false); // 종료
    saveChallenge(host, ChallengeType.PUBLIC, today.minusDays(1), today.plusDays(3), true); // 삭제

    List<Challenge> result =
        challengeRepository.findRandomActiveChallenges(today, PageRequest.of(0, 50));

    assertThat(result).extracting(Challenge::getId).contains(active.getId(), unlimited.getId());
    assertThat(result).hasSize(2);
  }

  @Test
  void findRandomPublicDiaries_excludesPrivateAndDeleted() {
    Member m = memberRepository.save(Member.builder().email("m@t.com").build());
    Challenge c =
        saveChallenge(m, ChallengeType.PUBLIC, today.minusDays(1), today.plusDays(3), false);
    diaryRepository.save(
        Diary.builder()
            .member(m)
            .challenge(c)
            .completedDate(today)
            .isPublic(true)
            .isDeleted(false)
            .build());
    diaryRepository.save(
        Diary.builder()
            .member(m)
            .challenge(c)
            .completedDate(today)
            .isPublic(false)
            .isDeleted(false)
            .build()); // 비공개 제외
    diaryRepository.save(
        Diary.builder()
            .member(m)
            .challenge(c)
            .completedDate(today)
            .isPublic(true)
            .isDeleted(true)
            .build()); // 삭제 제외

    List<Diary> result = diaryRepository.findRandomPublicDiaries(PageRequest.of(0, 50));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getIsPublic()).isTrue();
    assertThat(result.get(0).getIsDeleted()).isFalse();
  }

  @Test
  void countTodayInProgressGoals_countsOnlyMembersInProgressGoals() {
    Member me = memberRepository.save(Member.builder().email("me@t.com").build());
    Member other = memberRepository.save(Member.builder().email("o@t.com").build());

    Challenge inProgress =
        saveChallenge(me, ChallengeType.PUBLIC, today.minusDays(1), today.plusDays(3), false);
    Challenge ended =
        saveChallenge(me, ChallengeType.PUBLIC, today.minusDays(9), today.minusDays(1), false);

    saveGoals(me, inProgress, 2); // 카운트 대상
    saveGoals(me, ended, 3); // 종료 → 제외
    saveGoals(other, inProgress, 4); // 타인 → 제외

    assertThat(challengeGoalRepository.countTodayInProgressGoals(me.getId(), today)).isEqualTo(2);
  }

  @Test
  void countByDiaryId_countsLikes() {
    Member m = memberRepository.save(Member.builder().email("l@t.com").build());
    Member m2 = memberRepository.save(Member.builder().email("l2@t.com").build());
    Challenge c =
        saveChallenge(m, ChallengeType.PUBLIC, today.minusDays(1), today.plusDays(3), false);
    Diary d =
        diaryRepository.save(
            Diary.builder()
                .member(m)
                .challenge(c)
                .completedDate(today)
                .isPublic(true)
                .isDeleted(false)
                .build());

    diaryLikeRepository.save(DiaryLike.builder().diary(d).member(m).build());
    diaryLikeRepository.save(DiaryLike.builder().diary(d).member(m2).build());

    assertThat(diaryLikeRepository.countByDiaryId(d.getId())).isEqualTo(2);
  }

  private void saveGoals(Member member, Challenge challenge, int n) {
    Participant p =
        participantRepository.save(
            Participant.builder()
                .status(ParticipantStatus.PARTICIPANT)
                .member(member)
                .challenge(challenge)
                .build());
    for (int i = 0; i < n; i++) {
      challengeGoalRepository.save(ChallengeGoal.builder().content("g" + i).participant(p).build());
    }
  }
}
