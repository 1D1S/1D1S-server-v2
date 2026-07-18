package com.odos.odos_server_v2.domain.vote.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.vote.dto.AdminVoteDetailResponse;
import com.odos.odos_server_v2.domain.vote.dto.AdminVoteOptionResponse;
import com.odos.odos_server_v2.domain.vote.dto.AdminVoteVoterResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteCreateRequest;
import com.odos.odos_server_v2.domain.vote.dto.VoteDetailResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteOptionResultResponse;
import com.odos.odos_server_v2.domain.vote.dto.VoteSubmitRequest;
import com.odos.odos_server_v2.domain.vote.dto.VoteSummaryResponse;
import com.odos.odos_server_v2.domain.vote.entity.Vote;
import com.odos.odos_server_v2.domain.vote.entity.VoteOption;
import com.odos.odos_server_v2.domain.vote.entity.VoteResponse;
import com.odos.odos_server_v2.domain.vote.entity.VoteSelectionType;
import com.odos.odos_server_v2.domain.vote.entity.VoteType;
import com.odos.odos_server_v2.domain.vote.repository.VoteRepository;
import com.odos.odos_server_v2.domain.vote.repository.VoteResponseRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final VoteRepository voteRepository;
  private final VoteResponseRepository voteResponseRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public VoteSummaryResponse create(VoteCreateRequest request) {
    validateCreate(request);
    Vote vote =
        Vote.create(
            request.getTitle().trim(),
            request.getSelectionType(),
            request.getVoteType(),
            request.getStartDate(),
            request.getEndDate(),
            request.getOptions().stream().map(String::trim).toList());
    return VoteSummaryResponse.from(voteRepository.save(vote), false);
  }

  public List<VoteSummaryResponse> getPublicVotes(Long memberId) {
    return voteRepository.findAllByVoteTypeOrderByStartDateDescIdDesc(VoteType.PUBLIC).stream()
        .map(v -> VoteSummaryResponse.from(v, hasVoted(v.getId(), memberId)))
        .toList();
  }

  /** 오늘 노출되는 설문에는 결과 비공개 관리자 설문도 포함된다. */
  public List<VoteSummaryResponse> getTodayVotes(Long memberId) {
    return voteRepository.findAllOpenVotes(LocalDate.now(KST)).stream()
        .map(v -> VoteSummaryResponse.from(v, hasVoted(v.getId(), memberId)))
        .toList();
  }

  public List<VoteSummaryResponse> getAllVotesForAdmin() {
    return voteRepository.findAllByOrderByStartDateDescIdDesc().stream()
        .map(v -> VoteSummaryResponse.from(v, false))
        .toList();
  }

  public VoteDetailResponse getUserDetail(Long voteId, Long memberId) {
    Vote vote = getVote(voteId);
    List<VoteResponse> responses =
        voteResponseRepository.findAllByVoteIdOrderByCreatedAtAsc(voteId);
    return detail(vote, memberId, responses, vote.getVoteType() == VoteType.PUBLIC);
  }

  public VoteDetailResponse getPublicResult(Long voteId, Long memberId) {
    Vote vote = getVote(voteId);
    if (vote.getVoteType() != VoteType.PUBLIC) {
      throw new CustomException(ErrorCode.VOTE_RESULT_PRIVATE);
    }
    List<VoteResponse> responses =
        voteResponseRepository.findAllByVoteIdOrderByCreatedAtAsc(voteId);
    return detail(vote, memberId, responses, true);
  }

  public AdminVoteDetailResponse getAdminDetail(Long voteId) {
    Vote vote = getVote(voteId);
    List<VoteResponse> responses =
        voteResponseRepository.findAllByVoteIdOrderByCreatedAtAsc(voteId);
    long participantCount = responses.size();
    List<AdminVoteOptionResponse> options =
        vote.getOptions().stream()
            .map(option -> adminOption(option, responses, participantCount))
            .toList();
    return AdminVoteDetailResponse.of(vote, participantCount, options);
  }

  private AdminVoteOptionResponse adminOption(
      VoteOption option, List<VoteResponse> responses, long participantCount) {
    List<AdminVoteVoterResponse> voters =
        responses.stream()
            .filter(
                response ->
                    response.getSelectedOptions().stream()
                        .anyMatch(selected -> selected.getId().equals(option.getId())))
            .map(
                response ->
                    AdminVoteVoterResponse.builder()
                        .memberId(response.getMember().getId())
                        .nickname(response.getMember().getNickname())
                        .build())
            .toList();
    return AdminVoteOptionResponse.builder()
        .optionId(option.getId())
        .text(option.getText())
        .selectedCount(voters.size())
        .percentage(percentage(voters.size(), participantCount))
        .voters(voters)
        .build();
  }

  @Transactional
  public VoteDetailResponse submit(Long voteId, Long memberId, VoteSubmitRequest request) {
    Vote vote = getVote(voteId);
    if (!vote.isOpenOn(LocalDate.now(KST))) {
      throw new CustomException(ErrorCode.VOTE_NOT_OPEN);
    }
    if (voteResponseRepository.existsByVoteIdAndMemberId(voteId, memberId)) {
      throw new CustomException(ErrorCode.VOTE_ALREADY_SUBMITTED);
    }
    List<Long> optionIds = request.getOptionIds();
    Set<Long> uniqueIds = new HashSet<>(optionIds);
    if (uniqueIds.isEmpty()
        || uniqueIds.size() != optionIds.size()
        || (vote.getSelectionType() == VoteSelectionType.SINGLE && uniqueIds.size() != 1)) {
      throw new CustomException(ErrorCode.INVALID_VOTE_SELECTION);
    }
    Map<Long, VoteOption> optionMap =
        vote.getOptions().stream()
            .collect(Collectors.toMap(VoteOption::getId, Function.identity()));
    if (!optionMap.keySet().containsAll(uniqueIds)) {
      throw new CustomException(ErrorCode.INVALID_VOTE_OPTION);
    }
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    List<VoteOption> selected = optionIds.stream().map(optionMap::get).toList();
    voteResponseRepository.save(VoteResponse.create(vote, member, selected));
    List<VoteResponse> responses =
        voteResponseRepository.findAllByVoteIdOrderByCreatedAtAsc(voteId);
    return detail(vote, memberId, responses, vote.getVoteType() == VoteType.PUBLIC);
  }

  private VoteDetailResponse detail(
      Vote vote, Long memberId, List<VoteResponse> responses, boolean exposePercentage) {
    long participantCount = responses.size();
    Map<Long, Long> counts =
        responses.stream()
            .flatMap(r -> r.getSelectedOptions().stream())
            .collect(Collectors.groupingBy(VoteOption::getId, Collectors.counting()));
    List<VoteOptionResultResponse> options =
        vote.getOptions().stream()
            .map(
                option ->
                    VoteOptionResultResponse.builder()
                        .optionId(option.getId())
                        .text(option.getText())
                        .percentage(
                            exposePercentage
                                ? percentage(
                                    counts.getOrDefault(option.getId(), 0L), participantCount)
                                : null)
                        .build())
            .toList();
    return VoteDetailResponse.of(vote, hasVoted(vote.getId(), memberId), options);
  }

  private BigDecimal percentage(long selectedCount, long participantCount) {
    if (participantCount == 0) {
      return BigDecimal.ZERO.setScale(2);
    }
    return BigDecimal.valueOf(selectedCount)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
  }

  private boolean hasVoted(Long voteId, Long memberId) {
    return memberId != null && voteResponseRepository.existsByVoteIdAndMemberId(voteId, memberId);
  }

  private Vote getVote(Long voteId) {
    return voteRepository
        .findById(voteId)
        .orElseThrow(() -> new CustomException(ErrorCode.VOTE_NOT_FOUND));
  }

  private void validateCreate(VoteCreateRequest request) {
    if (request.getStartDate().isAfter(request.getEndDate())) {
      throw new CustomException(ErrorCode.INVALID_VOTE_PERIOD);
    }
    List<String> normalized = request.getOptions().stream().map(String::trim).toList();
    if (new HashSet<>(normalized).size() != normalized.size()) {
      throw new CustomException(ErrorCode.DUPLICATE_VOTE_OPTION);
    }
  }
}
