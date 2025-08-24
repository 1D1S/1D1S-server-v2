package com.odos.odos_server_v2.domain.challenge.controller;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeRequest;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ParticipantResponse;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
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
}
