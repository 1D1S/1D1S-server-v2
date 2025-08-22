package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeService {
  private final ParticipantRepository participantRepository;
  private final ChallengeLikeRepository challengeLikeRepository;
  private final ChallengeGoalRepository challengeGoalRepository;
  private final DiaryGoalRepository diaryGoalRepository;
  private final DiaryRepository diaryRepository;
  private final ImageService imageService;

  private ChallengeResponse toChallengeResponse(Challenge challenge, Member member) {
    Long challengeId = challenge.getId();
    Long memberId = member.getId();
    // 챌린지 목표
    List<ChallengeGoal> challengeGoals;
    if (challenge.getType() == ChallengeType.FIXED) {
      challengeGoals =
          participantRepository
              .findByMemberIdAndChallengeId(challenge.getHostMember().getId(), challengeId)
              .getChallengeGoals();
    } else {
      ParticipantStatus status = getMemberStatus(challengeId, memberId);
      if ((status == ParticipantStatus.HOST) || (status == ParticipantStatus.PARTICIPANT)) {
        challengeGoals =
            participantRepository
                .findByMemberIdAndChallengeId(memberId, challengeId)
                .getChallengeGoals();
      } else {
        challengeGoals = Collections.emptyList();
      }
    }
    // 참여자
    List<ParticipantStatus> participantStatuses;
    if (challenge.getHostMember().getId().equals(member.getId())) {
      participantStatuses =
          List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT, ParticipantStatus.PENDING);
    } else {
      participantStatuses = List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT);
    }
    List<Participant> participants =
        participantRepository.findByChallengeIdAndStatusIn(challengeId, participantStatuses);
    Participant participant =
        participantRepository.findByMemberIdAndChallengeId(member.getId(), challenge.getId());
    return new ChallengeResponse(
        toChallengeSummary(challenge, memberId),
        toChallengeDetail(challenge, memberId),
        challengeGoals.stream().map(this::toChallengeGoal).toList(),
        participants.stream().map(this::toParticipant).toList());
  }

  private ChallengeSummaryResponse toChallengeSummary(Challenge challenge, Long memberId) {
    Long challengeId = challenge.getId();
    LikeDto likeInfo =
        new LikeDto(
            challengeLikeRepository.existsByChallengeIdAndMemberId(challengeId, memberId),
            challengeLikeRepository.countByChallengeId(challengeId));
    return new ChallengeSummaryResponse(
        challengeId,
        challenge.getTitle(),
        challenge.getCategory(),
        challenge.getStartDate(),
        challenge.getEndDate(),
        challenge.getMaxParticipantsCnt(),
        challenge.getType(),
        getParticipantCnt(challengeId),
        likeInfo);
  }

  private ChallengeDetailDto toChallengeDetail(Challenge challenge, Long memberId) {
    return new ChallengeDetailDto(
        challenge.getDescription(),
        getMemberStatus(challenge.getId(), memberId),
        getParticipationRate(challenge),
        getGoalCompletionRate(challenge));
  }

  private final ChallengeGoalDto toChallengeGoal(ChallengeGoal challengeGoal) {
    return new ChallengeGoalDto(challengeGoal.getId(), challengeGoal.getContent());
  }

  private final ParticipantDto toParticipant(Participant participant) {
    Member member = participant.getMember();
    return new ParticipantDto(
        member.getId(),
        participant.getId(),
        member.getNickname(),
        imageService.getFileUrl(member.getProfileUrl()),
        participant.getStatus());
  }

  private long getParticipantCnt(Long challengeId) {
    return participantRepository.countByChallengeIdAndStatusIn(
        challengeId, List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT));
  }

  private ParticipantStatus getMemberStatus(Long challengeId, Long memberId) {
    Participant participant =
        participantRepository.findByMemberIdAndChallengeId(memberId, challengeId);
    if (participant == null) {
      return ParticipantStatus.NONE;
    }
    return participant.getStatus();
  }

  private double getParticipationRate(Challenge challenge) {
    LocalDate startDate = challenge.getStartDate();
    LocalDate endDate = challenge.getEndDate();
    LocalDate today = LocalDate.now();
    if (today.isBefore(startDate)) return -1;

    Long challengeId = challenge.getId();
    long allGoalsCompletedDiaryCnt =
        diaryRepository.countByChallengeIdAndIsAllGoalsCompletedTrue(challengeId);

    long participantCnt = getParticipantCnt(challengeId);
    if (participantCnt == 0) return 0;

    long days;
    if (today.isBefore(endDate)) {
      days = ChronoUnit.DAYS.between(startDate, today) + 1; // 오늘 포함
    } else {
      days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // 종료일 포함
    }
    if (days == 0) return 0;

    return (double) allGoalsCompletedDiaryCnt / ((double) participantCnt * (double) days);
  }

  private double getGoalCompletionRate(Challenge challenge) {
    if (challenge.getType() == ChallengeType.FLEXIBLE) return -1;
    LocalDate startDate = challenge.getStartDate();
    LocalDate endDate = challenge.getEndDate();
    LocalDate today = LocalDate.now();
    if (today.isBefore(startDate)) return -1;

    Long challengeId = challenge.getId();

    // 수행된 목표 수
    long completedGoalCnt =
        diaryGoalRepository.countByDiary_Challenge_IdAndIsCompletedTrue(challengeId);
    // 전체 참여자
    long participantCnt = getParticipantCnt(challengeId);
    // 진행된 일 수
    long days;
    if (today.isBefore(endDate)) {
      days = ChronoUnit.DAYS.between(startDate, today) + 1; // 오늘 포함
    } else {
      days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // 종료일 포함
    }
    if (days == 0) return 0;
    // 목표의 개수
    Participant participant =
        participantRepository.findByMemberIdAndChallengeId(
            challenge.getHostMember().getId(), challengeId);
    long goalCnt = challengeGoalRepository.countByParticipantId(participant.getId());
    if (goalCnt <= 0) return 0;

    return completedGoalCnt / ((double) participantCnt * (double) days * (double) goalCnt) * 100.0;
  }
}
