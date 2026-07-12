package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * V36 마이그레이션(중복 dedupe → 유니크 제약)을 실 Postgres + 실 Flyway 로 검증. (도커 필요)
 *
 * <p>V35 까지 적용한 스키마에 중복 데이터를 심고 V36 을 적용해: (1) participant 중복이 winner(MIN id)만 남고, (2) 삭제된 중복의
 * challenge_goal 이 winner 로 재지정되며, (3) challenge_like / diary_like 중복이 제거되고, (4) 이후 중복 삽입이 유니크 제약으로
 * 실패하는지 확인한다.
 *
 * <p>스키마에 FK 제약이 없어 부모 행(member/challenge/diary) 없이 정수 FK 만으로 자식 행을 심을 수 있다.
 */
@Testcontainers
class ParticipantLikeDedupeMigrationPostgresTest {

  private static final String SCHEMA = "odos_dev";

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("odos")
          .withUsername("odos")
          .withPassword("odos");

  private Flyway flyway(String target) {
    var cfg =
        Flyway.configure()
            .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
            .schemas(SCHEMA)
            .defaultSchema(SCHEMA)
            .createSchemas(true)
            .locations("classpath:db/migration");
    if (target != null) {
      cfg = cfg.target(target);
    }
    return cfg.load();
  }

  private Connection conn() throws SQLException {
    Connection c =
        DriverManager.getConnection(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    try (Statement s = c.createStatement()) {
      s.execute("SET search_path TO " + SCHEMA);
    }
    return c;
  }

  private long scalar(Connection c, String sql) throws SQLException {
    try (Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(sql)) {
      rs.next();
      return rs.getLong(1);
    }
  }

  @Test
  void v36_dedupesDuplicatesReassignsGoalsAndEnforcesUnique() throws Exception {
    // 1) V35 까지 적용(유니크 제약 도입 전 상태)
    flyway("35").migrate();

    try (Connection c = conn();
        Statement s = c.createStatement()) {
      // participant: (m1,c1) 중복 2건(id 1,2), (m1,c2) 1건(id 3), (member NULL,c1) 1건(id 4, dedupe 제외)
      s.execute(
          "INSERT INTO participant (id, status, member_id, challenge_id) VALUES "
              + "(1,'PARTICIPANT',1,1),(2,'PARTICIPANT',1,1),(3,'HOST',1,2),(4,'PARTICIPANT',NULL,1)");
      // challenge_goal: winner(1) 소유 1건(id 10), loser(2) 소유 2건(id 11,12) → 재지정 대상
      s.execute(
          "INSERT INTO challenge_goal (id, content, participant_id) VALUES "
              + "(10,'g-keep',1),(11,'g-dup1',2),(12,'g-dup2',2)");
      // challenge_like: (c1,m1) 중복 2건 + (c1,m2) 1건
      s.execute(
          "INSERT INTO challenge_like (id, challenge_id, member_id) VALUES "
              + "(1,1,1),(2,1,1),(3,1,2)");
      // diary_like: (d1,m1) 중복 2건 + (d2,m1) 1건
      s.execute(
          "INSERT INTO diary_like (id, diary_id, member_id) VALUES " + "(1,1,1),(2,1,1),(3,2,1)");
    }

    // 2) V36 적용(dedupe + 유니크)
    flyway(null).migrate();

    try (Connection c = conn()) {
      // participant: (m1,c1) 은 winner(id 1)만 남고, (m1,c2)·(NULL,c1) 은 그대로 → 총 3행
      assertThat(scalar(c, "SELECT count(*) FROM participant WHERE member_id=1 AND challenge_id=1"))
          .isEqualTo(1);
      assertThat(scalar(c, "SELECT id FROM participant WHERE member_id=1 AND challenge_id=1"))
          .isEqualTo(1);
      assertThat(scalar(c, "SELECT count(*) FROM participant")).isEqualTo(3);

      // challenge_goal: loser(2)의 목표가 winner(1)로 재지정 → 3건 모두 participant_id=1
      assertThat(scalar(c, "SELECT count(*) FROM challenge_goal WHERE participant_id=1"))
          .isEqualTo(3);
      assertThat(scalar(c, "SELECT count(*) FROM challenge_goal WHERE participant_id=2"))
          .isEqualTo(0);

      // challenge_like / diary_like: 중복 제거
      assertThat(
              scalar(c, "SELECT count(*) FROM challenge_like WHERE challenge_id=1 AND member_id=1"))
          .isEqualTo(1);
      assertThat(scalar(c, "SELECT count(*) FROM challenge_like")).isEqualTo(2);
      assertThat(scalar(c, "SELECT count(*) FROM diary_like WHERE diary_id=1 AND member_id=1"))
          .isEqualTo(1);
      assertThat(scalar(c, "SELECT count(*) FROM diary_like")).isEqualTo(2);

      // 유니크 제약 강제: 이후 중복 삽입은 실패
      try (Statement s = c.createStatement()) {
        assertThatThrownBy(
                () ->
                    s.execute(
                        "INSERT INTO participant (status, member_id, challenge_id) VALUES ('PARTICIPANT',1,1)"))
            .isInstanceOf(SQLException.class);
      }
      try (Statement s = c.createStatement()) {
        assertThatThrownBy(
                () ->
                    s.execute("INSERT INTO challenge_like (challenge_id, member_id) VALUES (1,1)"))
            .isInstanceOf(SQLException.class);
      }
      try (Statement s = c.createStatement()) {
        assertThatThrownBy(
                () -> s.execute("INSERT INTO diary_like (diary_id, member_id) VALUES (1,1)"))
            .isInstanceOf(SQLException.class);
      }
    }
  }
}
