package com.odos.odos_server_v2.domain.notice.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notice.dto.NoticeCreateRequest;
import com.odos.odos_server_v2.domain.notice.dto.NoticeResponse;
import com.odos.odos_server_v2.domain.notice.dto.NoticeUpdateRequest;
import com.odos.odos_server_v2.domain.notice.entity.Notice;
import com.odos.odos_server_v2.domain.notice.repository.NoticeRepository;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 공지: 어드민 CRUD + 사용자 공개 조회. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final MemberRepository memberRepository;

  // --- 조회(공용) ---
  public OffsetPagination<NoticeResponse> getNotices(Pageable pageable) {
    return OffsetPagination.from(
        noticeRepository.findAllOrdered(pageable).map(NoticeResponse::from));
  }

  public NoticeResponse getNotice(Long id) {
    return NoticeResponse.from(findNotice(id));
  }

  // --- 어드민 ---
  @Transactional
  public NoticeResponse create(NoticeCreateRequest req, Long authorMemberId) {
    validateRequired(req.getTitle(), req.getContent());
    Member author =
        memberRepository
            .findById(authorMemberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Notice saved =
        noticeRepository.save(
            Notice.create(
                req.getTitle(), req.getContent(), Boolean.TRUE.equals(req.getPinned()), author));
    return NoticeResponse.from(saved);
  }

  @Transactional
  public NoticeResponse update(Long id, NoticeUpdateRequest req) {
    Notice notice = findNotice(id);
    notice.update(req.getTitle(), req.getContent(), req.getPinned());
    return NoticeResponse.from(notice);
  }

  @Transactional
  public void delete(Long id) {
    noticeRepository.delete(findNotice(id));
  }

  private Notice findNotice(Long id) {
    return noticeRepository
        .findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
  }

  private void validateRequired(String title, String content) {
    if (isBlank(title) || isBlank(content)) {
      throw new CustomException(ErrorCode.NOTICE_REQUIRED_FIELD_MISSING);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
