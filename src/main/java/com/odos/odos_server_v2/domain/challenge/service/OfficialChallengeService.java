package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.FixedChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.FixedChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
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
  private final FixedChallengeGoalRepository fixedChallengeGoalRepository;
  private final ParticipantRepository participantRepository;
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
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
            .visibleFrom(request.getVisibleFrom())
            .password(null)
            .build();

    challengeRepository.save(challenge);

    // 공식 챌린지: 호스트를 participant 로 등록하지 않는다. (host_member_id 만 생성자로 남긴다.)
    // 고정목표 챌린지의 목표는 fixed_challenge_goal 에 저장하고, 참여자가 들어올 때 challenge_goal 로 복제된다.
    if (challenge.getGoalType() == GoalType.FIXED && request.getGoals() != null) {
      for (String g : request.getGoals()) {
        fixedChallengeGoalRepository.save(
            FixedChallengeGoal.builder().content(g).challenge(challenge).build());
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

      // 고정목표: 챌린지 원본 목표(fixed_challenge_goal)도 함께 갱신한다.
      if (challenge.getGoalType().equals(GoalType.FIXED)) {
        fixedChallengeGoalRepository.deleteAllByChallengeId(challengeId);
        request
            .getGoals()
            .ifPresent(
                goals ->
                    fixedChallengeGoalRepository.saveAll(
                        goals.stream()
                            .map(
                                goal ->
                                    FixedChallengeGoal.builder()
                                        .challenge(challenge)
                                        .content(goal)
                                        .build())
                            .toList()));
      }

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
    // 예약 노출 시각: 필드 생략=변경 없음, null=예약 해제(즉시 노출), 값=예약 설정/변경.
    if (request.getVisibleFrom() != null) {
      challenge.updateVisibleFrom(request.getVisibleFrom().orElse(null));
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
