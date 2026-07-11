package com.odos.odos_server_v2.domain.diary.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 일지 작성 유예 기간(챌린지 endDate + 2일, KST) 검증 단위 테스트.
 *
 * <p>가드는 참여자 조회보다 먼저 실행되므로: 차단이면 DIARY-011 을, 허용이면 가드를 통과해 이후 단계의 PARTICIPANT_NOT_FOUND 를 던진다. 이
 * 차이로 가드의 허용/차단 결정을 가볍게 검증한다. (DB 불필요)
 */
@ExtendWith(MockitoExtension.class)
class DiaryServiceWriteWindowTest {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  @Mock MemberRepository memberRepository;
  @Mock ChallengeRepository challengeRepository;
  @Mock ChallengeService challengeService;
  @Mock ParticipantRepository participantRepository;
  @InjectMocks DiaryService diaryService;

  private void stub(LocalDate endDate) {
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mock(Member.class)));
    when(challengeRepository.findById(1L))
        .thenReturn(Optional.of(Challenge.builder().endDate(endDate).build()));
  }

  private DiaryRequest request() {
    DiaryRequest r = new DiaryRequest();
    r.setChallengeId(1L);
    return r;
  }

  private void assertBlocked(LocalDate endDate) {
    stub(endDate);
    assertThatThrownBy(() -> diaryService.createDiary(1L, request()))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.DIARY_WRITE_PERIOD_CLOSED);
  }

  private void assertPassesWindowGuard(LocalDate endDate) {
    // 가드 통과 시 이후 참여자 조회에서 PARTICIPANT_NOT_FOUND 로 떨어진다(= 창구는 열려 있음).
    stub(endDate);
    assertThatThrownBy(() -> diaryService.createDiary(1L, request()))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.PARTICIPANT_NOT_FOUND);
  }

  @Test
  void blocked_whenPastEndDatePlusGrace() {
    assertBlocked(LocalDate.now(KST).minusDays(3)); // lastWritable = today-1 < today → 차단
  }

  @Test
  void allowed_onLastGraceDay_boundary() {
    LocalDate today = LocalDate.now(KST);
    assertPassesWindowGuard(today.minusDays(2)); // lastWritable = today → 허용(경계 포함)
  }

  @Test
  void allowed_whenOngoing() {
    assertPassesWindowGuard(LocalDate.now(KST)); // 아직 진행/종료 당일 → 허용
  }

  @Test
  void allowed_whenUnlimited_nullEndDate() {
    assertPassesWindowGuard(null); // 무기한 챌린지 → 항상 허용
  }
}
