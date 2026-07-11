package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeDailyCountProjection;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
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

/** 챌린지 통계(날짜별 추이) + 챌린지 일지 날짜 필터 쿼리를 실 Postgres + 실 Flyway 로 검증. (도커 필요) */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChallengeStatisticsQueryPostgresTest {

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
  @Autowired DiaryRepository diaryRepository;

  private Long challengeId;

  @BeforeEach
  void setUp() {
    Member host = memberRepository.save(Member.builder().email("host@t.com").build());
    Challenge ch =
        challengeRepository.save(
            Challenge.builder()
                .title("통계 테스트 챌린지")
                .hostMember(host)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 5))
                .challengeType(ChallengeType.PUBLIC)
                .goalType(GoalType.FIXED)
                .participationType(ParticipationType.GROUP)
                .build());
    challengeId = ch.getId();

    saveDiary(host, ch, LocalDate.of(2026, 7, 1), false, true);
    saveDiary(host, ch, LocalDate.of(2026, 7, 1), false, false); // 같은 날 2건(1건 비공개)
    saveDiary(host, ch, LocalDate.of(2026, 7, 2), false, true);
    saveDiary(host, ch, LocalDate.of(2026, 7, 2), true, true); // 삭제 → 제외
    saveDiary(host, ch, LocalDate.of(2026, 7, 6), false, true); // 기간 밖(추이 범위 제외)
  }

  @Test
  void countDiariesByDateForChallenge_excludesDeletedAndRespectsRange() {
    List<ChallengeDailyCountProjection> rows =
        diaryRepository.countDiariesByDateForChallenge(
            challengeId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5));
    Map<LocalDate, Long> byDate =
        rows.stream()
            .collect(
                Collectors.toMap(
                    ChallengeDailyCountProjection::getBucket,
                    ChallengeDailyCountProjection::getCnt));

    assertThat(byDate).containsEntry(LocalDate.of(2026, 7, 1), 2L); // 공개+비공개 모두
    assertThat(byDate).containsEntry(LocalDate.of(2026, 7, 2), 1L); // 삭제 1건 제외
    assertThat(byDate).doesNotContainKey(LocalDate.of(2026, 7, 6)); // 범위 밖
  }

  @Test
  void findAllByChallengeIdAndCompletedDate_returnsThatDateOnly() {
    var page =
        diaryRepository.findAllByChallengeIdAndCompletedDateAndIsDeletedFalse(
            challengeId, LocalDate.of(2026, 7, 1), PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(2); // 07-01 공개+비공개
  }

  @Test
  void findPublicByChallengeIdAndCompletedDate_returnsPublicOnly() {
    var page =
        diaryRepository.findByChallengeIdAndIsPublicAndCompletedDateAndIsDeletedFalse(
            challengeId, Boolean.TRUE, LocalDate.of(2026, 7, 1), PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(1); // 07-01 공개만
  }

  private void saveDiary(
      Member m, Challenge ch, LocalDate date, boolean deleted, boolean isPublic) {
    diaryRepository.save(
        Diary.builder()
            .member(m)
            .challenge(ch)
            .completedDate(date)
            .isDeleted(deleted)
            .isPublic(isPublic)
            .build());
  }
}
