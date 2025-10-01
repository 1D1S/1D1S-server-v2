package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import com.odos.odos_server_v2.domain.shared.dto.PageInfo;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {
  private final ParticipantRepository participantRepository;
  private final ChallengeLikeRepository challengeLikeRepository;
  private final ChallengeGoalRepository challengeGoalRepository;
  private final DiaryGoalRepository diaryGoalRepository;
  private final DiaryRepository diaryRepository;
  private final ImageService imageService;
  private final ChallengeRepository challengeRepository;
  private final MemberRepository memberRepository;
  private final CursorService cursorService;

  @Transactional
  public ChallengeSummaryResponse createChallenge(
      ChallengeRequest challengeRequest, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Challenge challenge =
        Challenge.builder()
            .title(challengeRequest.getTitle())
            .category(challengeRequest.getCategory())
            .startDate(challengeRequest.getStartDate())
            .endDate(challengeRequest.getEndDate())
            .maxParticipantsCnt(challengeRequest.getMaxParticipantCnt())
            .type(challengeRequest.getChallengeType())
            .description(challengeRequest.getDescription())
            .hostMember(member)
            .build();

    challengeRepository.save(challenge);
    Participant participant =
        Participant.builder()
            .member(member)
            .challenge(challenge)
            .status(ParticipantStatus.HOST)
            .build();
    participantRepository.save(participant);
    for (String g : challengeRequest.getGoals()) {
      ChallengeGoal challengeGoal =
          ChallengeGoal.builder().content(g).participant(participant).build();
      challengeGoalRepository.save(challengeGoal);
    }
    return toChallengeSummary(challenge, memberId);
  }

  public ChallengeResponse getChallenge(Long challengeId, Long memberId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return toChallengeResponse(challenge, member);
  }

  @Transactional
  public ParticipantResponse applyParticipant(Long challengeId, Long memberId, List<String> goals) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if (participantRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
      throw new CustomException(ErrorCode.ALREADY_APPLIED);
    }
    Participant participant =
        Participant.builder()
            .member(member)
            .challenge(challenge)
            .status(ParticipantStatus.PENDING)
            .build();
    participantRepository.save(participant);

    if (challenge.getType().equals(ChallengeType.FLEXIBLE)) {
      for (String g : goals) {
        ChallengeGoal goal = ChallengeGoal.builder().content(g).participant(participant).build();
        challengeGoalRepository.save(goal);
      }
    } else {
      Participant hostParticipant =
          participantRepository.findByMemberIdAndChallengeId(
              challenge.getHostMember().getId(), challengeId);
      List<ChallengeGoal> challengeGoals = hostParticipant.getChallengeGoals();
      for (ChallengeGoal cg : challengeGoals) {
        challengeGoalRepository.save(
            ChallengeGoal.builder().participant(participant).content(cg.getContent()).build());
      }
    }
    return toParticipant(participant);
  }

  @Transactional
  public void acceptParticipant(Long participantId, Long memberId) {
    Participant participant =
        participantRepository
            .findById(participantId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
    if (participant.getChallenge().getHostMember().getId().equals(memberId)) {
      participant.setStatus(ParticipantStatus.PARTICIPANT);
      participantRepository.save(participant);
    } else {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
  }

  @Transactional
  public void rejectParticipant(Long participantId, Long memberId) {
    Participant participant =
        participantRepository
            .findById(participantId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
    if (participant.getChallenge().getHostMember().getId().equals(memberId)) {
      participant.setStatus(ParticipantStatus.REJECTED);
      participantRepository.save(participant);
    } else {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
  }

  public List<ChallengeSummaryResponse> getRandomChallenges(Long memberId, int size) {
    List<Challenge> all = challengeRepository.findAll();
    Collections.shuffle(all);
    return all.stream().limit(size).map(ch -> toChallengeSummary(ch, memberId)).toList();
  }

  @Transactional
  public void leaveChallenge(Long memberId, Long challengeId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    // 호스트라면 탈퇴할 수 없음, 추후 수정 필요
    if (challenge.getHostMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.CANNOT_LEAVE_CHALLENGE);
    } else {
      Participant participant =
          participantRepository.findByMemberIdAndChallengeId(memberId, challengeId);
      if (participant == null) {
        throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND);
      }
      // 일지 소프트 딜리트 필요
      participant.setStatus(ParticipantStatus.LEAVE);
    }
  }

  public Pagination<ChallengeSummaryResponse> getChallengeList(
      Long memberId, int limit, String cursor, String keyword) {
    String kw = (keyword == null) ? "" : keyword.trim();
    Long cursorId =
        (cursor == null || cursor.isBlank()) ? null : cursorService.decodeCursorToId(cursor);

    Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "id"));
    List<Challenge> rows = challengeRepository.searchPage(cursorId, kw, pageable);

    boolean hasNext = rows.size() > limit;
    if (hasNext) {
      rows = rows.subList(0, limit);
    }

    List<ChallengeSummaryResponse> items =
        rows.stream()
            .map(c -> toChallengeSummary(c, memberId)) // 네가 이미 쓰는 메서드
            .toList();

    String nextCursor = null;
    if (hasNext && !rows.isEmpty()) {
      Long lastId = rows.get(rows.size() - 1).getId();
      nextCursor = cursorService.encodeCursor(lastId);
    }
    PageInfo pageInfo = new PageInfo();
    pageInfo.setLimit((long) limit);
    pageInfo.setHasNextPage(hasNext);
    pageInfo.setNextCursor(nextCursor);

    Pagination<ChallengeSummaryResponse> result = new Pagination<>();
    result.setItems(items);
    result.setPageInfo(pageInfo);
    return result;
  }

  @Transactional
  public void addChallengeLike(Long memberId, Long challengeId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (challengeLikeRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
      throw new CustomException(ErrorCode.ALREADY_LIKED);
    }
    ChallengeLike challengeLike =
        ChallengeLike.builder().challenge(challenge).member(member).build();
    challengeLikeRepository.save(challengeLike);
  }

  @Transactional
  public void cancelChallengeLike(Long memberId, Long challengeId) {
    ChallengeLike challengeLike =
        challengeLikeRepository.findByChallengeIdAndMemberId(challengeId, memberId);
    if (challengeLike == null) {
      throw new CustomException(ErrorCode.CHALLENGE_LIKE_NOT_FOUND);
    }
    challengeLikeRepository.delete(challengeLike);
  }

  public List<ChallengeSummaryResponse> getMemberChallenge(Long currentMemberId, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return member.getChallenges().stream()
        .map(ch -> toChallengeSummary(ch, currentMemberId))
        .toList();
  }

  public ChallengeResponse toChallengeResponse(Challenge challenge, Member member) {
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

  public ChallengeSummaryResponse toChallengeSummary(Challenge challenge, Long memberId) {
    Long challengeId = challenge.getId();
    LikeDto likeInfo;
    if (memberId != null) {
      likeInfo =
          new LikeDto(
              challengeLikeRepository.existsByChallengeIdAndMemberId(challengeId, memberId),
              challengeLikeRepository.countByChallengeId(challengeId));

    } else {
      likeInfo = new LikeDto(false, challengeLikeRepository.countByChallengeId(challengeId));
    }
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

  private final ParticipantResponse toParticipant(Participant participant) {
    Member member = participant.getMember();
    String profileUrl;
    if (member.getProfileUrl() == null) {
      profileUrl = "";
    } else {
      profileUrl = imageService.getFileUrl(member.getProfileUrl());
    }
    return new ParticipantResponse(
        member.getId(),
        participant.getId(),
        member.getNickname(),
        profileUrl,
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

    return (double) allGoalsCompletedDiaryCnt / ((double) participantCnt * (double) days) * 100;
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
