package com.odos.odos_server_v2.domain.notice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notice.dto.NoticeCreateRequest;
import com.odos.odos_server_v2.domain.notice.dto.NoticeResponse;
import com.odos.odos_server_v2.domain.notice.dto.NoticeUpdateRequest;
import com.odos.odos_server_v2.domain.notice.entity.Notice;
import com.odos.odos_server_v2.domain.notice.repository.NoticeRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

  @Mock private NoticeRepository noticeRepository;
  @Mock private MemberRepository memberRepository;

  @InjectMocks private NoticeService noticeService;

  private final Member admin = Member.builder().id(1L).build();

  @Test
  void create_savesNoticeWithGivenFields() {
    NoticeCreateRequest req = new NoticeCreateRequest();
    ReflectionTestUtils.setField(req, "title", "점검 안내");
    ReflectionTestUtils.setField(req, "content", "내용입니다.");
    ReflectionTestUtils.setField(req, "pinned", true);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(admin));
    when(noticeRepository.save(any(Notice.class))).thenAnswer(inv -> inv.getArgument(0));

    NoticeResponse response = noticeService.create(req, 1L);

    assertEquals("점검 안내", response.title());
    assertEquals("내용입니다.", response.content());
    assertTrue(response.pinned());
  }

  @Test
  void create_defaultsPinnedToFalseWhenNull() {
    NoticeCreateRequest req = new NoticeCreateRequest();
    ReflectionTestUtils.setField(req, "title", "제목");
    ReflectionTestUtils.setField(req, "content", "내용");
    when(memberRepository.findById(1L)).thenReturn(Optional.of(admin));
    when(noticeRepository.save(any(Notice.class))).thenAnswer(inv -> inv.getArgument(0));

    NoticeResponse response = noticeService.create(req, 1L);

    assertFalse(response.pinned());
  }

  @Test
  void create_throwsWhenTitleBlank() {
    NoticeCreateRequest req = new NoticeCreateRequest();
    ReflectionTestUtils.setField(req, "title", "  ");
    ReflectionTestUtils.setField(req, "content", "내용");

    CustomException exception =
        assertThrows(CustomException.class, () -> noticeService.create(req, 1L));

    assertEquals(ErrorCode.NOTICE_REQUIRED_FIELD_MISSING, exception.getErrorCode());
    verify(noticeRepository, never()).save(any());
    verify(memberRepository, never()).findById(any());
  }

  @Test
  void create_throwsWhenAuthorNotFound() {
    NoticeCreateRequest req = new NoticeCreateRequest();
    ReflectionTestUtils.setField(req, "title", "제목");
    ReflectionTestUtils.setField(req, "content", "내용");
    when(memberRepository.findById(99L)).thenReturn(Optional.empty());

    CustomException exception =
        assertThrows(CustomException.class, () -> noticeService.create(req, 99L));

    assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    verify(noticeRepository, never()).save(any());
  }

  @Test
  void update_appliesOnlyProvidedFields() {
    Notice notice = Notice.create("원제목", "원내용", false, admin);
    when(noticeRepository.findById(10L)).thenReturn(Optional.of(notice));
    NoticeUpdateRequest req = new NoticeUpdateRequest();
    ReflectionTestUtils.setField(req, "pinned", true); // title/content 는 null → 유지

    NoticeResponse response = noticeService.update(10L, req);

    assertEquals("원제목", response.title());
    assertEquals("원내용", response.content());
    assertTrue(response.pinned());
  }

  @Test
  void delete_throwsWhenNotFound() {
    when(noticeRepository.findById(404L)).thenReturn(Optional.empty());

    CustomException exception =
        assertThrows(CustomException.class, () -> noticeService.delete(404L));

    assertEquals(ErrorCode.NOTICE_NOT_FOUND, exception.getErrorCode());
    verify(noticeRepository, never()).delete(any());
  }
}
