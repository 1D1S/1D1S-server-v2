package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfficialChallengeService {

  private final ChallengeRepository challengeRepository;
  private final ChallengeGoalRepository challengeGoalRepository;
  private final ParticipantRepository participantRepository;
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
  private final NotificationService notificationService;
  private final ChallengeService challengeService;

  private void checkOfficialAuthority(Member member) {
    if (member.getRole().equals(MemberRole.ADMIN)) {
      return;
    }
    throw new CustomException(ErrorCode.OFFICIAL_CHALLENGE_NOT_AUTHORIZED);
  }

  @Transactional
  public ChallengeSummaryResponse createOfficialChallenge(
      OfficialChallengeRequest request, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // TODO: 공식 챌린지 생성 권한 기획 완료 후 아래 임시 닉네임 체크 로직을 권한 시스템으로 교체해야 합니다.
    checkOfficialAuthority(member);

    Challenge challenge =
        Challenge.builder()
            .title(request.getTitle())
            .thumbnailImage(request.getThumbnailImage())
            .category(request.getCategory())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .maxParticipantsCnt(null)
            .goalType(request.getGoalType())
            .description(request.getDescription())
            .hostMember(member)
            .allowMidJoin(request.getAllowMidJoin() != null && request.getAllowMidJoin())
            .participationType(ParticipationType.GROUP)
            .challengeType(ChallengeType.OFFICIAL)
            .password(null)
            .build();

    challengeRepository.save(challenge);

    Participant participant =
        Participant.builder()
            .member(member)
            .challenge(challenge)
            .status(ParticipantStatus.HOST)
            .build();
    participantRepository.save(participant);

    notificationService.notifyChallengeApproved(
        memberId, participant.getMember().getId(), challenge.getId(), challenge.getTitle());

    if (request.getGoals() != null) {
      for (String g : request.getGoals()) {
        challengeGoalRepository.save(
            ChallengeGoal.builder().content(g).participant(participant).build());
      }
    }

    return challengeService.toChallengeSummary(challenge, memberId);
  }

  @Transactional
  public ChallengeSummaryResponse editOfficialChallenge(
      Long challengeId, OfficialChallengeEditRequest request, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    checkOfficialAuthority(member);

    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    if (challenge.getChallengeType() != ChallengeType.OFFICIAL) {
      throw new CustomException(ErrorCode.CHALLENGE_NOT_OFFICIAL);
    }

    if (!challenge.getHostMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.CHALLENGE_NOT_ACCESS);
    }

    if (request.getGoals() != null) {
      if (!challenge.getStartDate().isAfter(LocalDate.now())) {
        throw new CustomException(ErrorCode.CANNOT_EDIT_CHALLENGE_GOALS);
      }

      List<ParticipantStatus> participantStatuses;
      if (challenge.getGoalType().equals(GoalType.FIXED)) {
        participantStatuses = List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT);
      } else {
        participantStatuses = List.of(ParticipantStatus.HOST);
      }
      List<Participant> participants =
          participantRepository.findByChallengeIdAndStatusIn(challengeId, participantStatuses);

      challengeGoalRepository.deleteAllByParticipantIn(participants);

      request
          .getGoals()
          .ifPresent(
              goals -> {
                List<ChallengeGoal> newGoals =
                    participants.stream()
                        .flatMap(
                            p ->
                                goals.stream()
                                    .map(
                                        goal ->
                                            ChallengeGoal.builder()
                                                .participant(p)
                                                .content(goal)
                                                .build()))
                        .toList();
                challengeGoalRepository.saveAll(newGoals);
              });
    }

    if (request.getTitle() != null) {
      challenge.updateTitle(request.getTitle().orElse(null));
    }
    if (request.getThumbnailImage() != null) {
      challenge.updateThumbnailImage(request.getThumbnailImage().orElse(null));
    }
    if (request.getCategory() != null) {
      challenge.updateCategory(request.getCategory().orElse(null));
    }
    if (request.getDescription() != null) {
      challenge.updateDescription(request.getDescription().orElse(null));
    }
    if (request.getAllowMidJoin() != null) {
      challenge.updateAllowMidJoin(request.getAllowMidJoin().orElse(false));
    }

    return challengeService.toChallengeSummary(challenge, memberId);
  }

  @Transactional
  public void deleteOfficialChallenge(Long challengeId, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    checkOfficialAuthority(member);

    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    if (challenge.getChallengeType() != ChallengeType.OFFICIAL) {
      throw new CustomException(ErrorCode.CHALLENGE_NOT_OFFICIAL);
    }

    if (!challenge.getHostMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.CHALLENGE_NOT_ACCESS);
    }

    challenge.softDelete();
    diaryRepository.softDeleteByChallengeId(challengeId);
  }
}
