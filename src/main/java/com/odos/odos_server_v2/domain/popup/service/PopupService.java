package com.odos.odos_server_v2.domain.popup.service;

import com.odos.odos_server_v2.domain.popup.dto.PopupAdminResponse;
import com.odos.odos_server_v2.domain.popup.dto.PopupCreateRequest;
import com.odos.odos_server_v2.domain.popup.dto.PopupResponse;
import com.odos.odos_server_v2.domain.popup.dto.PopupUpdateRequest;
import com.odos.odos_server_v2.domain.popup.entity.Popup;
import com.odos.odos_server_v2.domain.popup.repository.PopupRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 홈 팝업: 어드민 CRUD + 클라 게시중 조회. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final PopupRepository popupRepository;

  // --- 클라(홈) ---
  /** 오늘(KST) 게시 중인 활성 팝업 목록(시작일 오름차순). */
  public List<PopupResponse> getActivePopups() {
    return popupRepository.findActiveOn(LocalDate.now(KST)).stream()
        .map(PopupResponse::from)
        .toList();
  }

  // --- 어드민 ---
  @Transactional
  public PopupAdminResponse create(PopupCreateRequest req) {
    validateRequired(req);
    validatePeriod(req.getStartDate(), req.getEndDate());
    Popup saved =
        popupRepository.save(
            Popup.create(
                req.getImageUrl(),
                req.getCtaText(),
                req.getLinkUrl(),
                req.getTitle(),
                req.getStartDate(),
                req.getEndDate()));
    return PopupAdminResponse.from(saved);
  }

  @Transactional
  public PopupAdminResponse update(Long id, PopupUpdateRequest req) {
    Popup popup =
        popupRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.POPUP_NOT_FOUND));
    popup.update(
        req.getImageUrl(),
        req.getCtaText(),
        req.getLinkUrl(),
        req.getTitle(),
        req.getStartDate(),
        req.getEndDate(),
        req.getIsActive());
    // 부분 수정 반영 후의 최종 기간으로 정합성 검증.
    validatePeriod(popup.getStartDate(), popup.getEndDate());
    return PopupAdminResponse.from(popup);
  }

  @Transactional
  public void delete(Long id) {
    Popup popup =
        popupRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.POPUP_NOT_FOUND));
    popupRepository.delete(popup);
  }

  public PopupAdminResponse getOne(Long id) {
    return PopupAdminResponse.from(
        popupRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.POPUP_NOT_FOUND)));
  }

  /** 어드민 달력 뷰: [from, to] 와 게시기간이 겹치는 팝업(from/to 선택, null 이면 전체). */
  public List<PopupAdminResponse> getForAdmin(LocalDate from, LocalDate to) {
    return popupRepository.findForAdminCalendar(from, to).stream()
        .map(PopupAdminResponse::from)
        .toList();
  }

  private void validateRequired(PopupCreateRequest req) {
    if (isBlank(req.getImageUrl())
        || isBlank(req.getCtaText())
        || isBlank(req.getLinkUrl())
        || req.getStartDate() == null
        || req.getEndDate() == null) {
      throw new CustomException(ErrorCode.POPUP_REQUIRED_FIELD_MISSING);
    }
  }

  private void validatePeriod(LocalDate start, LocalDate end) {
    if (start == null || end == null || start.isAfter(end)) {
      throw new CustomException(ErrorCode.INVALID_POPUP_PERIOD);
    }
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
