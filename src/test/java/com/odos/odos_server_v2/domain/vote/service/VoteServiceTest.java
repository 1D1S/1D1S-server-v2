package com.odos.odos_server_v2.domain.vote.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.vote.dto.AdminVoteDetailResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteDetailResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteSubmitRequest;
import com.odos.odos_server_v2.domain.vote.entity.Vote;
import com.odos.odos_server_v2.domain.vote.entity.VoteOption;
import com.odos.odos_server_v2.domain.vote.entity.VoteResponse;
import com.odos.odos_server_v2.domain.vote.entity.VoteSelectionType;
import com.odos.odos_server_v2.domain.vote.entity.VoteType;
import com.odos.odos_server_v2.domain.vote.repository.VoteRepository;
import com.odos.odos_server_v2.domain.vote.repository.VoteResponseRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {
  @Mock VoteRepository voteRepository;
  @Mock VoteResponseRepository voteResponseRepository;
  @Mock MemberRepository memberRepository;

  private VoteService voteService;

  @BeforeEach
  void setUp() {
    voteService = new VoteService(voteRepository, voteResponseRepository, memberRepository);
  }

  @Test
  void singleVoteRejectsMultipleOptions() {
    Vote vote = openVote(VoteSelectionType.SINGLE, VoteType.PUBLIC);
    when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));

    assertThatThrownBy(() -> voteService.submit(1L, 10L, new VoteSubmitRequest(List.of(11L, 12L))))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_VOTE_SELECTION);
    verify(voteResponseRepository, never()).save(any());
  }

  @Test
  void privateSurveyDetailDoesNotExposePercentages() {
    Vote vote = openVote(VoteSelectionType.MULTIPLE, VoteType.ADMIN_SURVEY);
    when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
    when(voteResponseRepository.findAllByVoteIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

    VoteDetailResponse response = voteService.getUserDetail(1L, 10L);

    assertThat(response.getOptions()).allMatch(option -> option.getPercentage() == null);
  }

  @Test
  void privateSurveyResultEndpointIsForbidden() {
    Vote vote = openVote(VoteSelectionType.SINGLE, VoteType.ADMIN_SURVEY);
    when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));

    assertThatThrownBy(() -> voteService.getPublicResult(1L, 10L))
        .isInstanceOf(CustomException.class)
        .extracting(e -> ((CustomException) e).getErrorCode())
        .isEqualTo(ErrorCode.VOTE_RESULT_PRIVATE);
  }

  @Test
  void adminDetailGroupsVotersByOption() {
    Vote vote = openVote(VoteSelectionType.MULTIPLE, VoteType.PUBLIC);
    Member firstMember = Member.builder().id(10L).nickname("첫 번째 회원").build();
    Member secondMember = Member.builder().id(20L).nickname("두 번째 회원").build();
    VoteResponse firstResponse =
        VoteResponse.builder()
            .vote(vote)
            .member(firstMember)
            .selectedOptions(List.of(vote.getOptions().get(0), vote.getOptions().get(1)))
            .build();
    VoteResponse secondResponse =
        VoteResponse.builder()
            .vote(vote)
            .member(secondMember)
            .selectedOptions(List.of(vote.getOptions().get(1)))
            .build();
    when(voteRepository.findById(1L)).thenReturn(Optional.of(vote));
    when(voteResponseRepository.findAllByVoteIdOrderByCreatedAtAsc(1L))
        .thenReturn(List.of(firstResponse, secondResponse));

    AdminVoteDetailResponse response = voteService.getAdminDetail(1L);

    assertThat(response.getParticipantCount()).isEqualTo(2);
    assertThat(response.getOptions().get(0).getSelectedCount()).isEqualTo(1);
    assertThat(response.getOptions().get(0).getPercentage()).isEqualByComparingTo("50.00");
    assertThat(response.getOptions().get(0).getVoters())
        .extracting("memberId")
        .containsExactly(10L);
    assertThat(response.getOptions().get(1).getSelectedCount()).isEqualTo(2);
    assertThat(response.getOptions().get(1).getPercentage()).isEqualByComparingTo("100.00");
    assertThat(response.getOptions().get(1).getVoters())
        .extracting("memberId")
        .containsExactly(10L, 20L);
  }

  private Vote openVote(VoteSelectionType selectionType, VoteType voteType) {
    VoteOption first = VoteOption.builder().id(11L).text("첫 번째").displayOrder(0).build();
    VoteOption second = VoteOption.builder().id(12L).text("두 번째").displayOrder(1).build();
    return Vote.builder()
        .id(1L)
        .title("테스트 투표")
        .selectionType(selectionType)
        .voteType(voteType)
        .startDate(LocalDate.now().minusDays(1))
        .endDate(LocalDate.now().plusDays(1))
        .options(List.of(first, second))
        .build();
  }
}
