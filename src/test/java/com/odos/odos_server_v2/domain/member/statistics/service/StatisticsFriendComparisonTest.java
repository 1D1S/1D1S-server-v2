package com.odos.odos_server_v2.domain.member.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.member.statistics.StatUnit;
import com.odos.odos_server_v2.domain.member.statistics.dto.FriendComparisonResponse;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository;
import com.odos.odos_server_v2.domain.member.statistics.repository.StatisticsRepository.MemberCount;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 특정 친구 1:1 비교 단위 테스트. (DB 불필요) */
@ExtendWith(MockitoExtension.class)
class StatisticsFriendComparisonTest {

  @Mock StatisticsRepository statisticsRepository;
  @Mock MemberRepository memberRepository;
  @Mock FriendRepository friendRepository;
  @Mock ImageService imageService;
  @InjectMocks StatisticsService service;

  private static MemberCount mc(long memberId, long cnt) {
    return new MemberCount() {
      @Override
      public Long getMemberId() {
        return memberId;
      }

      @Override
      public long getCnt() {
        return cnt;
      }
    };
  }

  @Test
  void returnsMeAndFriendStats_whenValidFriend() {
    Member me = mock(Member.class);
    Member friend = mock(Member.class);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(me));
    when(memberRepository.findById(2L)).thenReturn(Optional.of(friend));
    when(friendRepository.existsByMemberAndFriendMember(me, friend)).thenReturn(true);
    when(friend.getNickname()).thenReturn("친구닉");
    when(friend.getProfileUrl()).thenReturn("key.png");
    when(imageService.getFileUrl("key.png")).thenReturn("https://cdn/key.png");
    when(statisticsRepository.countDiariesByMembers(any(), any(), any()))
        .thenReturn(List.of(mc(1L, 5), mc(2L, 3)));
    when(statisticsRepository.countCompletedGoalsByMembers(any(), any(), any()))
        .thenReturn(List.of(mc(1L, 2), mc(2L, 4)));

    FriendComparisonResponse r = service.getFriendComparison(1L, StatUnit.MONTH, 2L);

    assertThat(r.period()).isEqualTo(StatUnit.MONTH);
    assertThat(r.me().diaryCount()).isEqualTo(5);
    assertThat(r.me().completedGoalCount()).isEqualTo(2);
    assertThat(r.friend().memberId()).isEqualTo(2L);
    assertThat(r.friend().nickname()).isEqualTo("친구닉");
    assertThat(r.friend().profileUrl()).isEqualTo("https://cdn/key.png");
    assertThat(r.friend().diaryCount()).isEqualTo(3);
    assertThat(r.friend().completedGoalCount()).isEqualTo(4);
  }

  @Test
  void throwsFriendNotExists_whenNotMyFriend() {
    Member me = mock(Member.class);
    Member other = mock(Member.class);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(me));
    when(memberRepository.findById(9L)).thenReturn(Optional.of(other));
    when(friendRepository.existsByMemberAndFriendMember(me, other)).thenReturn(false);

    assertThatThrownBy(() -> service.getFriendComparison(1L, StatUnit.MONTH, 9L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.FRIEND_NOT_EXISTS);
  }

  @Test
  void throwsInvalidPeriod_whenNotWeekOrMonth() {
    // period 검증이 회원/친구 조회보다 먼저라 stub 불필요.
    lenient().when(memberRepository.findById(any())).thenReturn(Optional.of(mock(Member.class)));
    assertThatThrownBy(() -> service.getFriendComparison(1L, StatUnit.YEAR, 2L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_STATISTICS_PERIOD);
  }
}
