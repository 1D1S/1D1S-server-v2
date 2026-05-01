package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryStreakResponse;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.dto.PageInfo;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
  private final NotificationService notificationService;

  @Transactional
  public ChallengeSummaryResponse createChallenge(
      ChallengeRequest challengeRequest, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (challengeRequest.getParticipationType().equals(ParticipationType.GROUP)
        && challengeRequest.getMaxParticipantCnt() < 2) {
      throw new CustomException(ErrorCode.INVALID_CHALLENGE_REQUEST);
    }

    Challenge challenge =
        Challenge.builder()
            .title(challengeRequest.getTitle())
            .thumbnailImage(challengeRequest.getThumbnailImage())
            .category(challengeRequest.getCategory())
            .startDate(challengeRequest.getStartDate())
            .endDate(challengeRequest.getEndDate())
            .maxParticipantsCnt(challengeRequest.getMaxParticipantCnt())
            .goalType(challengeRequest.getGoalType())
            .description(challengeRequest.getDescription())
            .hostMember(member)
            .allowMidJoin(challengeRequest.getAllowMidJoin())
            .participationType(challengeRequest.getParticipationType())
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
        participant.getMember().getId(), challenge.getId(), challenge.getTitle());

    for (String g : challengeRequest.getGoals()) {
      ChallengeGoal challengeGoal =
          ChallengeGoal.builder().content(g).participant(participant).build();
      challengeGoalRepository.save(challengeGoal);
    }
    return toChallengeSummary(challenge, memberId);
  }

  @Transactional
  public ChallengeSummaryResponse editChallenge(
      Long challengeId, ChallengeEditRequest challengeEditRequest, Long memberId) {
    memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    if (!challenge.getHostMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.CHALLENGE_NOT_ACCESS);
    }

    if (challengeEditRequest.getMaxParticipantCnt() != null) {
      challengeEditRequest
          .getMaxParticipantCnt()
          .ifPresent(
              newMax -> {
                if (newMax < getParticipantCnt(challengeId)) {
                  throw new CustomException(ErrorCode.MAX_PARTICIPANT);
                }
                challenge.updateMaxParticipantCnt(newMax);
              });
    }
    if (challengeEditRequest.getGoals() != null) {
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

      challengeEditRequest
          .getGoals()
          .ifPresent(
              goals -> {
                List<ChallengeGoal> newGoals =
                    participants.stream()
                        .flatMap(
                            participant ->
                                goals.stream()
                                    .map(
                                        goal ->
                                            ChallengeGoal.builder()
                                                .participant(participant)
                                                .content(goal)
                                                .build()))
                        .toList();
                challengeGoalRepository.saveAll(newGoals);
              });
    }
    if (challengeEditRequest.getTitle() != null) {
      challenge.updateTitle(challengeEditRequest.getTitle().orElse(null));
    }
    if (challengeEditRequest.getThumbnailImage() != null) {
      challenge.updateThumbnailImage(challengeEditRequest.getThumbnailImage().orElse(null));
    }
    if (challengeEditRequest.getCategory() != null) {
      challenge.updateCategory(challengeEditRequest.getCategory().orElse(null));
    }
    if (challengeEditRequest.getDescription() != null) {
      challenge.updateDescription(challengeEditRequest.getDescription().orElse(null));
    }
    if (challengeEditRequest.getAllowMidJoin() != null) {
      challenge.updateAllowMidJoin(challengeEditRequest.getAllowMidJoin().orElse(false));
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

  public OffsetPagination<ChallengeSummaryResponse> getChallengeListByOffset(
      Long memberId, int page, int size, String keyword, Category category) {

    Pageable pageable = PageRequest.of(page, size);

    Page<Challenge> challengePage = challengeRepository.findByFilters(keyword, category, pageable);

    Page<ChallengeSummaryResponse> responsePage =
        challengePage.map(challenge -> toChallengeSummary(challenge, memberId));

    return OffsetPagination.from(responsePage);
  }

  @Transactional
  public ParticipantResponse applyParticipant(Long challengeId, Long memberId, List<String> goals) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    if (challenge.getDeletedAt() != null) {
      throw new CustomException(ErrorCode.CANNOT_APPLY_PARTICIPANT);
    }
    if (challenge.getParticipationType().equals(ParticipationType.INDIVIDUAL)) {
      throw new CustomException(ErrorCode.CANNOT_APPLY_PARTICIPANT);
    }
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if (participantRepository.existsByChallengeIdAndMemberId(challengeId, memberId)) {
      throw new CustomException(ErrorCode.ALREADY_APPLIED);
    }
    if (!challenge.isAllowMidJoin() && challenge.getStartDate().isBefore(LocalDate.now())) {
      throw new CustomException(ErrorCode.CANNOT_APPLY_PARTICIPANT);
    }
    Participant participant =
        Participant.builder()
            .member(member)
            .challenge(challenge)
            .status(ParticipantStatus.PENDING)
            .build();
    participantRepository.save(participant);

    if (challenge.getGoalType().equals(GoalType.FLEXIBLE)) {
      for (String g : goals) {
        ChallengeGoal goal = ChallengeGoal.builder().content(g).participant(participant).build();
        challengeGoalRepository.save(goal);
      }
    } else {
      Participant hostParticipant =
          participantRepository
              .findByMemberIdAndChallengeId(challenge.getHostMember().getId(), challengeId)
              .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
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

    Challenge challenge = participant.getChallenge();
    if (!challenge.getHostMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    } else if (getParticipantCnt(challenge.getId()) >= challenge.getMaxParticipantsCnt()) {
      throw new CustomException(ErrorCode.CANNOT_ACCEPT_PARTICIPANT);
    }
    participant.setStatus(ParticipantStatus.PARTICIPANT);
    participantRepository.save(participant);
    notificationService.notifyChallengeApproved(
        participant.getMember().getId(), challenge.getId(), challenge.getTitle());
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

      notificationService.notifyChallengeRejected(
          participant.getMember().getId(),
          participant.getChallenge().getId(),
          participant.getChallenge().getTitle());

    } else {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
  }

  @Transactional
  public void editChallengeGoal(Long challengeId, Long memberId, List<String> goals) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    if (challenge.getGoalType().equals(GoalType.FIXED)) {
      throw new CustomException(ErrorCode.NO_AUTHORITY);
    }
    if (!challenge.getStartDate().isAfter(LocalDate.now())) {
      throw new CustomException(ErrorCode.CANNOT_EDIT_CHALLENGE_GOALS);
    }
    Participant participant =
        participantRepository
            .findByMemberIdAndChallengeId(memberId, challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));

    challengeGoalRepository.deleteAllByParticipant(participant);

    for (String g : goals) {
      ChallengeGoal goal = ChallengeGoal.builder().content(g).participant(participant).build();
      challengeGoalRepository.save(goal);
    }
  }

  public List<ChallengeSummaryResponse> getRandomChallenges(Long memberId, int size) {
    List<Challenge> all = challengeRepository.findAll();
    Collections.shuffle(all);
    return all.stream().limit(size).map(ch -> toChallengeSummary(ch, memberId)).toList();
  }

  public void withdrawMemberLeaveChallengeHost(Long memberId) {
    return;
  }

  public void withdrawMemberLeaveChallenge(Long memberId) {
    List<Participant> participants = participantRepository.findByMemberId(memberId);
    for (Participant p : participants) {
      leaveChallenge(memberId, p.getChallenge().getId());
    }
  }

  @Transactional
  public void rejoinMemberRestoreIndividualChallenge(Long memberId) {
    List<Challenge> challenges =
        challengeRepository.findByHostMemberIdAndParticipationTypeAndDeletedAtIsNotNull(
            memberId, ParticipationType.INDIVIDUAL);
    for (Challenge c : challenges) {
      if (c.getDeletedAt().isAfter(LocalDateTime.now().minusDays(7))) {
        c.restore();
        restoreMemberDiariesInChallenge(c.getId(), memberId);
      }
    }
  }

  @Transactional
  public void leaveChallenge(Long memberId, Long challengeId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    if (challenge.getParticipationType().equals(ParticipationType.INDIVIDUAL)) {
      // 개인 챌린지
      challenge.softDelete();
      diaryRepository.softDeleteByChallengeIdAndMemberId(challengeId, memberId);
    } else {
      // 단체 챌린지
      // 호스트라면 호스트를 넘김
      if (challenge.getHostMember().getId().equals(memberId)) {
        leaveChallengeHost(memberId, challengeId);
      } else {
        Participant participant =
            participantRepository
                .findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
        participant.setStatus(ParticipantStatus.LEAVE);
        diaryRepository.softDeleteByChallengeIdAndMemberId(challengeId, memberId);
      }
    }
  }

  public void leaveChallengeHost(Long memberId, Long challengeId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    List<Participant> participants =
        participantRepository.findByChallengeIdAndStatusIn(
            challengeId, List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT));

    // 호스트 본인 제외
    Participant currentHost =
        participants.stream()
            .filter(p -> p.getStatus() == ParticipantStatus.HOST)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("호스트를 찾을 수 없습니다"));

    List<Participant> candidates =
        participants.stream().filter(p -> p.getStatus() != ParticipantStatus.HOST).toList();

    if (candidates.isEmpty()) {
      challenge.softDelete();
      currentHost.setStatus(ParticipantStatus.LEAVE);
      softDeleteMemberDiariesInChallenge(challengeId, memberId);
      return;
    }

    Participant nextHost = null;
    long maxCount = -1;

    for (Participant p : candidates) {
      long count = countDiaryGoals(p.getId());

      if (count > maxCount) {
        maxCount = count;
        nextHost = p;
      } else if (count == maxCount) {
        // 동점이면 닉네임 가나다순 (사전순 앞)
        if (nextHost == null
            || p.getMember().getNickname().compareTo(nextHost.getMember().getNickname()) < 0) {
          nextHost = p;
        }
      }
    }

    // 권한 위임
    nextHost.setStatus(ParticipantStatus.HOST);
    currentHost.setStatus(ParticipantStatus.LEAVE);

    challenge.setHostMember(nextHost.getMember());

    participantRepository.save(nextHost);
    participantRepository.save(currentHost);
    challengeRepository.save(challenge);
    softDeleteMemberDiariesInChallenge(challengeId, memberId);
  }

  private long countDiaryGoals(Long participantId) {
    List<ChallengeGoal> challengeGoals = challengeGoalRepository.findByParticipantId(participantId);
    long total = 0;
    for (ChallengeGoal cg : challengeGoals) {
      total += diaryGoalRepository.countByChallengeGoalId(cg.getId());
    }
    return total;
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

    LocalDate now = LocalDate.now();

    List<ChallengeSummaryResponse> result =
        member.getParticipants().stream()
            .filter(
                p ->
                    (p.getStatus() == ParticipantStatus.HOST
                            || p.getStatus() == ParticipantStatus.PARTICIPANT)
                        && !p.getChallenge().getStartDate().isAfter(now)
                        && !p.getChallenge().getEndDate().isBefore(now))
            .map(Participant::getChallenge)
            .map(ch -> toChallengeSummary(ch, currentMemberId))
            .toList();

    return result;
  }

  public ChallengeResponse toChallengeResponse(Challenge challenge, Member member) {
    Long challengeId = challenge.getId();
    Long memberId = member.getId();
    // 챌린지 목표
    List<ChallengeGoal> challengeGoals;
    if (challenge.getGoalType() == GoalType.FIXED) {
      challengeGoals =
          participantRepository
              .findByMemberIdAndChallengeId(challenge.getHostMember().getId(), challengeId)
              .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND))
              .getChallengeGoals();
    } else {
      ParticipantStatus status = getMemberStatus(challengeId, memberId);
      if ((status == ParticipantStatus.HOST) || (status == ParticipantStatus.PARTICIPANT)) {
        challengeGoals =
            participantRepository
                .findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND))
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
        imageService.getFileUrl(challenge.getThumbnailImage()),
        challenge.getCategory(),
        challenge.getStartDate(),
        challenge.getEndDate(),
        challenge.getParticipationType(),
        challenge.getMaxParticipantsCnt(),
        challenge.getGoalType(),
        getParticipantCnt(challengeId),
        likeInfo,
        challenge.getDeletedAt() != null);
  }

  private ChallengeDetailDto toChallengeDetail(Challenge challenge, Long memberId) {
    return new ChallengeDetailDto(
        challenge.getDescription(),
        challenge.isAllowMidJoin(),
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
    Optional<Participant> participant =
        participantRepository.findByMemberIdAndChallengeId(memberId, challengeId);
    if (participant.isEmpty()) {
      return ParticipantStatus.NONE;
    } else {
      return participant.get().getStatus();
    }
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
    if (challenge.getGoalType() == GoalType.FLEXIBLE) return -1;
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
        participantRepository
            .findByMemberIdAndChallengeId(challenge.getHostMember().getId(), challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));
    long goalCnt = challengeGoalRepository.countByParticipantId(participant.getId());
    if (goalCnt <= 0) return 0;

    return completedGoalCnt / ((double) participantCnt * (double) days * (double) goalCnt) * 100.0;
  }

  @Transactional
  public DiaryStreakResponse getChallengeDiaryIn3Day(Long challengeId, Long currentMemberId) {
    memberRepository
        .findById(currentMemberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    challengeRepository
        .findById(challengeId)
        .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    LocalDate today = LocalDate.now();
    LocalDate twoDaysAgo = today.minusDays(2);
    List<Diary> diaries =
        diaryRepository.findDiariesByDateRange(twoDaysAgo, today, challengeId, currentMemberId);
    return DiaryStreakResponse.checkStreak(diaries);
  }

  private void softDeleteMemberDiariesInChallenge(Long challengeId, Long memberId) {
    diaryRepository.softDeleteByChallengeIdAndMemberId(challengeId, memberId);
  }

  private void restoreMemberDiariesInChallenge(Long challengeId, Long memberId) {
    diaryRepository.restoreByChallengeIdAndMemberId(challengeId, memberId);
  }
}
