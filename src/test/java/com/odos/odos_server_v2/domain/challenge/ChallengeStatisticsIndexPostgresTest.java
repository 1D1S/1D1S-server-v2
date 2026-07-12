package com.odos.odos_server_v2.domain.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
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
 * 챌린지 통계 성능 회귀(V34): 통계 쿼리는 모두 challenge_id 로 필터하므로 diary(challenge_id 선두)·diary_goal(diary_id)
 * 인덱스가 있어야 풀스캔을 피한다. 실 Postgres + 실 Flyway 로 마이그레이션이 인덱스를 생성했는지 검증한다.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChallengeStatisticsIndexPostgresTest {

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

  @Autowired EntityManager em;

  @Test
  void v34_createsChallengeStatisticsIndexes() {
    @SuppressWarnings("unchecked")
    List<String> indexes =
        em.createNativeQuery(
                "SELECT indexname FROM pg_indexes"
                    + " WHERE schemaname='odos_dev' AND tablename IN ('diary','diary_goal')")
            .getResultList();

    assertThat(indexes)
        .contains("idx_diary_challenge_deleted_completed", "idx_diary_goal_diary_completed");
  }
}
