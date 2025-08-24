package com.odos.odos_server_v2.domain.challenge.controller;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeRequest;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ParticipantResponse;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {
  private final ChallengeService challengeService;

  @PostMapping
  public ApiResponse<ChallengeSummaryResponse> createChallenge(
      @RequestBody ChallengeRequest challengeRequest) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.CREATE_CHALLENGE, challengeService.createChallenge(challengeRequest, memberId));
  }

  @GetMapping("/{challengeId}")
  public ApiResponse<ChallengeResponse> getChallenge(@PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_CHALLENGE, challengeService.getChallenge(challengeId, memberId));
  }

  @PostMapping("/{challengeId}/participants")
  public ApiResponse<ParticipantResponse> applyParticipation(
      @PathVariable Long challengeId, @RequestBody List<String> goals) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.APPLY_CHALLENGE, challengeService.applyParticipant(challengeId, memberId, goals));
  }

  @PatchMapping("/participants/{participantId}/accept")
  public ApiResponse<Void> acceptParticipant(@PathVariable Long participantId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.acceptParticipant(participantId, memberId);
    return ApiResponse.success(Message.ACCEPT_PARTICIPANT);
  }

  @PatchMapping("/participants/{participantId}/reject")
  public ApiResponse<Void> rejectParticipant(@PathVariable Long participantId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.rejectParticipant(participantId, memberId);
    return ApiResponse.success(Message.REJECT_PARTICIPANT);
  }

  @GetMapping("/random")
  public ApiResponse<List<ChallengeSummaryResponse>> randomChallenges(
      @RequestParam(name = "size", defaultValue = "10") int size) {
    Long memberId = CurrentUserContext.getCurrentMemberIdOrNull();
    return ApiResponse.success(
        Message.GET_RANDOM_CHALLENGES, challengeService.getRandomChallenges(memberId, size));
  }

  @DeleteMapping("/{challengeId}/participants")
  public ApiResponse<Void> leaveChallenge(@PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.leaveChallenge(memberId, challengeId);
    return ApiResponse.success(Message.LEAVE_CHALLENGE);
  }

  @GetMapping
  public ApiResponse<Pagination<ChallengeSummaryResponse>> challengeList(
      @RequestParam(name = "limit", defaultValue = "10") int limit,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "keyword", required = false) String keyword) {

    Long memberId = CurrentUserContext.getCurrentMemberId();
    Pagination<ChallengeSummaryResponse> page =
        challengeService.getChallengeList(memberId, limit, cursor, keyword);

    return ApiResponse.success(Message.GET_CHALLENGE_LIST, page);
  }

  @PostMapping("/{challengeId}/likes")
  public ApiResponse<Void> addChallengeLike(@PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.addChallengeLike(memberId, challengeId);
    return ApiResponse.success(Message.ADD_CHALLENGE_LIKE);
  }

  @DeleteMapping("/{challengeId}/likes")
  public ApiResponse<Void> cancelChallengeLike(@PathVariable Long challengeId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    challengeService.cancelChallengeLike(memberId, challengeId);
    return ApiResponse.success(Message.CANCEL_CHALLENGE_LIKE);
  }

  @GetMapping("/member")
  public ApiResponse<List<ChallengeSummaryResponse>> memberChallenges(
      @RequestParam("memberId") Long memberId) {
    Long currentMemberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.GET_CHALLENGES_BY_MEMBER,
        challengeService.getMemberChallenge(currentMemberId, memberId));
  }
}
