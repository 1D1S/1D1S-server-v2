package com.odos.odos_server_v2.domain.member.statistics.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import com.odos.odos_server_v2.domain.member.statistics.dto.PeriodSummaryResponse;
import com.odos.odos_server_v2.domain.member.statistics.service.StatisticsService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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
 * 통계 요약(summary) 경로를 <b>실 Postgres</b>(Testcontainers) + 실 Flyway 마이그레이션으로 검증하는 회귀 테스트.
 *
 * <p>배경: {@code aggregateFeelings} 의 optional 필터 {@code (:from is null or ...)} 는 H2 에선 통과하지만
 * Postgres 서버 프리페어 단계에서 "could not determine data type of parameter" 로 실패해 summary/feelings API 가
 * 500 을 냈다. 파라미터를 {@code cast(... as ...)} 로 타입 명시하여 수정했으며, 이 테스트가 재발을 막는다.
 *
 * <p>도커가 필요하므로 CI(build -x test)에서는 실행되지 않고 컴파일만 되며, 로컬/도커 환경에서 수행된다.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StatisticsSummaryPostgresTest {

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
  @Autowired StatisticsRepository statisticsRepository;
  @Autowired FriendRepository friendRepository;
  @Autowired EntityManager em;

  private StatisticsService service;
  private Long memberId;

  @BeforeEach
  void setUp() {
    service =
        new StatisticsService(
            statisticsRepository, memberRepository, friendRepository, mock(ImageService.class));

    Member m = memberRepository.save(Member.builder().email("pg-probe@t.com").build());
    memberId = m.getId();

    // 2026-W28 = 2026-07-06(월) ~ 2026-07-12(일)
    saveDiary(m, LocalDate.of(2026, 7, 6), Feeling.HAPPY); // 월요일 경계
    saveDiary(m, LocalDate.of(2026, 7, 7), Feeling.SAD);
    saveDiary(m, LocalDate.of(2026, 7, 7), Feeling.HAPPY); // 같은 날 2건
    saveDiary(m, LocalDate.of(2026, 7, 9), null); // feeling null → NONE
    saveDiary(m, LocalDate.of(2026, 7, 12), Feeling.NORMAL); // 일요일 경계
    saveDiary(m, LocalDate.of(2026, 7, 5), Feeling.HAPPY); // 직전 주(범위 밖)
    saveDiary(m, null, Feeling.HAPPY); // completedDate null 엣지

    // 가입일을 과거로 고정(실행 시점과 무관하게 안정적으로 통과하도록).
    em.createNativeQuery("update odos_dev.member set created_at = timestamp '2020-01-01 00:00:00'")
        .executeUpdate();
    em.flush();
    em.clear();
  }

  @Test
  void weekSummary_runsOnPostgresAndAggregatesCorrectly() {
    PeriodSummaryResponse r = service.getSummary(memberId, StatUnit.WEEK, "2026-W28");

    assertThat(r.start().toString()).isEqualTo("2026-07-06");
    assertThat(r.end().toString()).isEqualTo("2026-07-12");
    assertThat(r.diaryCount()).isEqualTo(5); // 07-06,07,07,09,12 (07-05·null 제외)
    assertThat(r.activeDays()).isEqualTo(4);
    assertThat(r.subTrend()).hasSize(7);
    assertThat(r.peakBucket().key()).isEqualTo("2026-07-07");
    assertThat(r.peakBucket().count()).isEqualTo(2);
    assertThat(r.feelingBreakdown().stream().mapToLong(s -> s.count()).sum()).isEqualTo(5);
  }

  @Test
  void feelingDistribution_withAllNullOptionalParams_runsOnPostgres() {
    // /feelings 엔드포인트 경로: from/to/challengeId 전부 null → optional 필터 cast 검증
    assertThatCode(() -> service.getFeelingDistribution(memberId, null, null, null))
        .doesNotThrowAnyException();
  }

  private void saveDiary(Member m, LocalDate date, Feeling feeling) {
    Diary d =
        Diary.builder().member(m).completedDate(date).feeling(feeling).isDeleted(false).build();
    DiaryGoal g = DiaryGoal.builder().diary(d).isCompleted(true).build();
    d.getDiaryGoals().add(g);
    statisticsRepository.saveAndFlush(d);
  }
}
