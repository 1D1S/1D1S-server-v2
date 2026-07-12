package com.odos.odos_server_v2.domain.popup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.odos.odos_server_v2.domain.popup.dto.PopupAdminResponse;
import com.odos.odos_server_v2.domain.popup.dto.PopupCreateRequest;
import com.odos.odos_server_v2.domain.popup.dto.PopupResponse;
import com.odos.odos_server_v2.domain.popup.dto.PopupUpdateRequest;
import com.odos.odos_server_v2.domain.popup.entity.Popup;
import com.odos.odos_server_v2.domain.popup.repository.PopupRepository;
import com.odos.odos_server_v2.domain.popup.service.PopupService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 홈 팝업: 게시 판정·CRUD·달력 조회를 실 Postgres + 실 Flyway(V33 포함) 로 검증. (도커 필요) */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PopupService.class)
class PopupPostgresTest {

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

  @Autowired PopupRepository popupRepository;
  @Autowired PopupService popupService;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private Popup savePopup(LocalDate start, LocalDate end, boolean active) {
    Popup p = Popup.create("https://cdn/img.png", "자세히", "https://link", "관리명", start, end);
    if (!active) {
      p.update(null, null, null, null, null, null, false);
    }
    return popupRepository.save(p);
  }

  @Test
  void getActivePopups_returnsOnlyActiveTodayInRange() {
    LocalDate today = LocalDate.now(KST);
    Popup inRange = savePopup(today.minusDays(1), today.plusDays(1), true); // 오늘 게시중
    savePopup(today.plusDays(1), today.plusDays(5), true); // 아직 시작 전
    savePopup(today.minusDays(5), today.minusDays(1), true); // 이미 종료
    savePopup(today.minusDays(1), today.plusDays(1), false); // 기간엔 맞지만 비활성

    List<PopupResponse> active = popupService.getActivePopups();

    assertThat(active).hasSize(1);
    assertThat(active.get(0).popupKey()).isEqualTo(inRange.getPopupKey());
    assertThat(active.get(0).imageUrl()).isEqualTo("https://cdn/img.png");
    assertThat(active.get(0).ctaText()).isEqualTo("자세히");
  }

  @Test
  void create_update_delete_flow() {
    PopupCreateRequest req = new PopupCreateRequest();
    req.setImageUrl("https://cdn/a.png");
    req.setCtaText("이벤트");
    req.setLinkUrl("https://l");
    req.setStartDate(LocalDate.of(2026, 7, 1));
    req.setEndDate(LocalDate.of(2026, 7, 10));
    PopupAdminResponse created = popupService.create(req);
    assertThat(created.popupKey()).isNotBlank();
    assertThat(created.isActive()).isTrue();

    PopupUpdateRequest upd = new PopupUpdateRequest();
    upd.setCtaText("변경됨");
    upd.setIsActive(false);
    PopupAdminResponse updated = popupService.update(created.id(), upd);
    assertThat(updated.ctaText()).isEqualTo("변경됨");
    assertThat(updated.isActive()).isFalse();
    assertThat(updated.linkUrl()).isEqualTo("https://l"); // 미변경 유지

    popupService.delete(created.id());
    assertThatThrownBy(() -> popupService.getOne(created.id()))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.POPUP_NOT_FOUND);
  }

  @Test
  void create_rejectsInvalidPeriodAndMissingFields() {
    PopupCreateRequest bad = new PopupCreateRequest();
    bad.setImageUrl("https://cdn/a.png");
    bad.setCtaText("t");
    bad.setLinkUrl("https://l");
    bad.setStartDate(LocalDate.of(2026, 7, 10));
    bad.setEndDate(LocalDate.of(2026, 7, 1)); // start > end
    assertThatThrownBy(() -> popupService.create(bad))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_POPUP_PERIOD);

    PopupCreateRequest missing = new PopupCreateRequest();
    missing.setCtaText("t"); // imageUrl/linkUrl/dates 누락
    assertThatThrownBy(() -> popupService.create(missing))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.POPUP_REQUIRED_FIELD_MISSING);
  }

  @Test
  void adminCalendar_overlapWithOptionalNullParams_runsOnPostgres() {
    savePopup(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), true); // A
    savePopup(LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 25), true); // B

    // from/to 모두 null(=cast 경로) → 전체
    assertThat(popupService.getForAdmin(null, null)).hasSize(2);

    // 7월 3일~7월 10일 창과 겹치는 것 = A 만
    List<PopupAdminResponse> julyEarly =
        popupService.getForAdmin(LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 10));
    assertThat(julyEarly).hasSize(1);
    assertThat(julyEarly.get(0).startDate()).isEqualTo(LocalDate.of(2026, 7, 1));

    // to 만 지정(from null) → 7/22 이전 시작 전부 = A, B 모두(B start 7/20 <= 7/22)
    assertThat(popupService.getForAdmin(null, LocalDate.of(2026, 7, 22))).hasSize(2);
  }
}
