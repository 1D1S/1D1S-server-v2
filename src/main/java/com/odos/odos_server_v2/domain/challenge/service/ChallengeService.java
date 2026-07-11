package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.*;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengePoke;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantSortType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.FixedChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeLikeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengePokeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.FixedChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryStreakResponse;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.dto.PageInfo;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.domain.shared.entity.MemberInfo;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {
  private final ParticipantRepository participantRepository;
  private final ChallengeLikeRepository challengeLikeRepository;
  private final ChallengeGoalRepository challengeGoalRepository;
  private final FixedChallengeGoalRepository fixedChallengeGoalRepository;
  private final ChallengePokeRepository challengePokeRepository;
  private final DiaryGoalRepository diaryGoalRepository;
  private final DiaryRepository diaryRepository;
  private final ImageService imageService;
  private final ChallengeRepository challengeRepository;
  private final MemberRepository memberRepository;
  private final CursorService cursorService;
  private final NotificationService notificationService;
  private final ChallengeRankingService challengeRankingService;
  @PersistenceContext private EntityManager entityManager;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public ChallengeSummaryResponse createChallenge(
      ChallengeRequest challengeRequest, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (challengeRequest.getParticipationType().equals(ParticipationType.GROUP)
        && challengeRequest.getMaxParticipantCnt() != null
        && challengeRequest.getMaxParticipantCnt() < 2) {
      throw new CustomException(ErrorCode.INVALID_CHALLENGE_REQUEST);
    }

    ChallengeType challengeType =
        challengeRequest.getChallengeType() != null
            ? challengeRequest.getChallengeType()
            : ChallengeType.PUBLIC;

    if (challengeType == ChallengeType.PRIVATE
        && (challengeRequest.getPassword() == null || challengeRequest.getPassword().isBlank())) {
      throw new CustomException(ErrorCode.PRIVATE_CHALLENGE);
    }

    String encodedPassword =
        challengeType == ChallengeType.PRIVATE
            ? passwordEncoder.encode(challengeRequest.getPassword())
            : null;

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
            .photoRequired(Boolean.TRUE.equals(challengeRequest.getPhotoRequired()))
            .postEndWriteAllowed(Boolean.TRUE.equals(challengeRequest.getPostEndWriteAllowed()))
            .participationType(challengeRequest.getParticipationType())
            .challengeType(challengeType)
            .password(encodedPassword)
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

    if (challenge.getGoalType() == GoalType.FIXED) {
      // 고정목표: 챌린지 단위 원본 목표(fixed_challenge_goal) 저장 후, 호스트(첫 참여자)에게 복제한다.
      for (String g : challengeRequest.getGoals()) {
        fixedChallengeGoalRepository.save(
            FixedChallengeGoal.builder().content(g).challenge(challenge).build());
        challengeGoalRepository.save(
            ChallengeGoal.builder().content(g).participant(participant).build());
      }
    } else {
      for (String g : challengeRequest.getGoals()) {
        challengeGoalRepository.save(
            ChallengeGoal.builder().content(g).participant(participant).build());
      }
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

    if (challengeEditRequest.getMaxParticipantCnt().isPresent()) {
      challenge.updateMaxParticipantCnt(challengeEditRequest.getMaxParticipantCnt().orElse(null));
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

      // 고정목표: 챌린지 원본 목표(fixed_challenge_goal)도 함께 갱신한다.
      if (challenge.getGoalType().equals(GoalType.FIXED)) {
        fixedChallengeGoalRepository.deleteAllByChallengeId(challengeId);
        challengeEditRequest
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

  public ChallengePreviewResponse getChallengePreview(Long challengeId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    return new ChallengePreviewResponse(
        challenge.getTitle(),
        challenge.getGoalType(),
        challenge.getParticipationType(),
        challenge.getChallengeType());
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

    if (challenge.getChallengeType() == ChallengeType.PRIVATE) {
      ParticipantStatus status = getMemberStatus(challengeId, memberId);
      if (status != ParticipantStatus.HOST && status != ParticipantStatus.PARTICIPANT) {
        throw new CustomException(ErrorCode.PRIVATE_CHALLENGE);
      }
    }

    return toChallengeResponse(challenge, member);
  }

  /** 특정 챌린지 통계: 참여율 + 완료 목표수 + 기간 내 날짜별 일지 추이. 권한은 챌린지 상세 조회 정책과 동일. */
  public ChallengeStatisticsResponse getChallengeStatistics(Long challengeId, Long memberId) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if (challenge.getChallengeType() == ChallengeType.PRIVATE) {
      ParticipantStatus status = getMemberStatus(challengeId, memberId);
      if (status != ParticipantStatus.HOST && status != ParticipantStatus.PARTICIPANT) {
        throw new CustomException(ErrorCode.PRIVATE_CHALLENGE);
      }
    }

    double participationRate = getParticipationRate(challenge);
    long completedGoalCount =
        diaryGoalRepository.countByDiary_Challenge_IdAndIsCompletedTrueAndDiary_IsDeletedFalse(
            challengeId);
    return new ChallengeStatisticsResponse(
        participationRate, completedGoalCount, buildChallengeDiaryTrend(challenge));
  }

  // 무기한(endDate==null) 챌린지 일지 추이의 최대 조회 구간(일). 오래된 무기한 챌린지에서 startDate~오늘 전 구간을
  // 일 단위로 순회하면 포인트 수·JSON 크기가 무한정 커져(오늘-시작일) 응답 지연/과부하로 이어진다. 최근 N일로 상한.
  private static final int UNLIMITED_TREND_MAX_DAYS = 90;

  // 챌린지 기간(startDate ~ endDate, 무기한이면 startDate ~ 오늘) 동안 날짜별 일지 개수 시계열(빈 날짜 0).
  // 무기한 챌린지는 최근 UNLIMITED_TREND_MAX_DAYS 일로 조회 구간을 제한(그 이전은 절삭).
  private List<ChallengeStatisticsResponse.DiaryTrendPoint> buildChallengeDiaryTrend(
      Challenge challenge) {
    LocalDate from = challenge.getStartDate();
    if (from == null) {
      return List.of();
    }
    LocalDate to = challenge.getEndDate() != null ? challenge.getEndDate() : LocalDate.now();
    if (to.isBefore(from)) {
      return List.of();
    }
    // 무기한: startDate 가 아주 오래됐어도 최근 N일 창으로 제한한다.
    if (challenge.getEndDate() == null) {
      LocalDate windowStart = to.minusDays(UNLIMITED_TREND_MAX_DAYS - 1L);
      if (windowStart.isAfter(from)) {
        from = windowStart;
      }
    }

    Map<LocalDate, Long> counts = new java.util.HashMap<>();
    for (ChallengeDailyCountProjection row :
        diaryRepository.countDiariesByDateForChallenge(challenge.getId(), from, to)) {
      counts.put(row.getBucket(), row.getCnt());
    }

    List<ChallengeStatisticsResponse.DiaryTrendPoint> out = new ArrayList<>();
    for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
      out.add(new ChallengeStatisticsResponse.DiaryTrendPoint(d, counts.getOrDefault(d, 0L)));
    }
    return out;
  }

  public OffsetPagination<ParticipantResponse> getChallengeParticipants(
      Long challengeId,
      Long memberId,
      List<ParticipantStatus> statusFilter,
      ParticipantSortType sort,
      int page,
      int size) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    // 조회 가능한 status 화이트리스트: 호스트/관리자만 참여 신청자(PENDING)까지 볼 수 있다.
    List<ParticipantStatus> allowed;
    if (isHostOrAdmin(challenge, memberId)) {
      allowed =
          List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT, ParticipantStatus.PENDING);
    } else {
      allowed = List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT);
    }

    // status 미지정이면 기존 동작(화이트리스트 전체), 지정이면 화이트리스트와 교집합(권한 밖 status는 조용히 제외).
    List<ParticipantStatus> effective;
    if (statusFilter == null || statusFilter.isEmpty()) {
      effective = allowed;
    } else {
      effective = statusFilter.stream().distinct().filter(allowed::contains).toList();
    }
    if (effective.isEmpty()) {
      // 요청한 status가 전부 권한 밖(예: 비호스트가 status=PENDING) → 빈 페이지
      return new OffsetPagination<>(
          List.of(), new OffsetPagination.PageInfo(page, size, 0, 0, false));
    }

    List<Participant> participants =
        participantRepository.findByChallengeIdAndStatusIn(challengeId, effective);

    // 등수는 승인된 참여자(HOST/PARTICIPANT)에만 의미가 있다.
    // PENDING 단독 조회면 랭킹 계산을 건너뛰고(성능) 참여순으로 고정하며 rank는 null.
    boolean rankable =
        effective.contains(ParticipantStatus.HOST)
            || effective.contains(ParticipantStatus.PARTICIPANT);
    Map<Long, ChallengeRankingService.RankInfo> ranks =
        rankable ? challengeRankingService.computeRanks(challengeId, participants) : Map.of();

    Comparator<Participant> order;
    if (rankable && sort == ParticipantSortType.RANK) {
      order =
          Comparator.comparingInt((Participant p) -> ranks.get(p.getId()).rank())
              .thenComparing(Participant::getId);
    } else {
      order = Comparator.comparing(Participant::getId); // 참여순(참여자 id 오름차순)
    }
    List<Participant> sorted = participants.stream().sorted(order).toList();

    int total = sorted.size();
    int safeSize = Math.max(size, 1);
    int from = (int) Math.min((long) Math.max(page, 0) * safeSize, total);
    int to = Math.min(from + safeSize, total);
    List<ParticipantResponse> items =
        sorted.subList(from, to).stream().map(p -> toParticipant(p, ranks.get(p.getId()))).toList();
    int totalPages = (int) Math.ceil((double) total / safeSize);
    return new OffsetPagination<>(
        items, new OffsetPagination.PageInfo(page, size, total, totalPages, to < total));
  }

  private boolean isHostOrAdmin(Challenge challenge, Long memberId) {
    if (memberId == null) {
      return false;
    }
    if (challenge.getHostMember().getId().equals(memberId)) {
      return true;
    }
    return memberRepository
        .findById(memberId)
        .map(member -> member.getRole() == MemberRole.ADMIN)
        .orElse(false);
  }

  /** 진행 상태 미선택 여부(=전체 조회). 이 경우 쿼리에서 날짜 조건을 우회한다. */
  private boolean isAllStatus(List<ChallengeStatus> statuses) {
    return statuses == null || statuses.isEmpty();
  }

  /**
   * 다중 선택 진행 상태를 쿼리용 이름 리스트로 변환. 미선택이면 세 상태 전체를 반환한다(IN 절이 빈 리스트가 되지 않도록 하는 용도이며, 실제 전체 조회는
   * allStatus 플래그로 처리).
   */
  private List<String> toStatusNames(List<ChallengeStatus> statuses) {
    if (isAllStatus(statuses)) {
      return List.of(
          ChallengeStatus.ONGOING.name(),
          ChallengeStatus.UPCOMING.name(),
          ChallengeStatus.ENDED.name());
    }
    return statuses.stream().distinct().map(ChallengeStatus::name).toList();
  }

  /** 카테고리 미선택 여부(=전체 조회). 이 경우 쿼리에서 카테고리 조건을 우회한다. */
  private boolean isAllCategory(List<Category> categories) {
    return categories == null || categories.isEmpty();
  }

  /** 다중 선택 카테고리를 쿼리용 이름 리스트로 변환. 미선택이면 전체를 반환한다(IN 절 빈 리스트 방지용, 실제 전체 조회는 allCategory 플래그로 처리). */
  private List<String> toCategoryNames(List<Category> categories) {
    if (isAllCategory(categories)) {
      return Arrays.stream(Category.values()).map(Category::name).toList();
    }
    return categories.stream().distinct().map(Category::name).toList();
  }

  public OffsetPagination<ChallengeSummaryResponse> getChallengeListByOffset(
      Long memberId,
      int page,
      int size,
      String keyword,
      List<Category> categories,
      ChallengeType challengeType,
      List<ChallengeStatus> statuses) {

    Pageable pageable = PageRequest.of(page, size);

    Page<Challenge> challengePage =
        challengeRepository.findByFilters(
            keyword,
            isAllCategory(categories),
            toCategoryNames(categories),
            ChallengeType.PRIVATE.name(),
            challengeType != null ? challengeType.name() : null,
            isAllStatus(statuses),
            toStatusNames(statuses),
            LocalDate.now(),
            pageable);

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
      if (goals == null || goals.isEmpty()) {
        throw new CustomException(ErrorCode.FLEXIBLE_GOAL_REQUIRED);
      }
      for (String g : goals) {
        ChallengeGoal goal = ChallengeGoal.builder().content(g).participant(participant).build();
        challengeGoalRepository.save(goal);
      }
    } else {
      // 고정목표: 챌린지의 원본 목표(fixed_challenge_goal)를 참여자의 challenge_goal 로 복제한다.
      for (FixedChallengeGoal fg : fixedChallengeGoalRepository.findByChallengeId(challengeId)) {
        challengeGoalRepository.save(
            ChallengeGoal.builder().participant(participant).content(fg.getContent()).build());
      }
    }

    notificationService.notifyChallengeApplied(
        member.getId(),
        challenge.getHostMember().getId(),
        challenge.getId(),
        member.getNickname(),
        challenge.getTitle());

    return toParticipant(participant);
  }

  @Transactional
  public ChallengeResponse verifyPasswordAndJoin(
      Long challengeId, Long memberId, String password, List<String> goals) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    if (challenge.getChallengeType() != ChallengeType.PRIVATE) {
      throw new CustomException(ErrorCode.CHALLENGE_NOT_PRIVATE);
    }
    if (!passwordEncoder.matches(password, challenge.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_CHALLENGE_PASSWORD);
    }
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
    if (challenge.getMaxParticipantsCnt() != null
        && getParticipantCnt(challengeId) >= challenge.getMaxParticipantsCnt()) {
      throw new CustomException(ErrorCode.CANNOT_ACCEPT_PARTICIPANT);
    }

    Participant participant =
        Participant.builder()
            .member(member)
            .challenge(challenge)
            .status(ParticipantStatus.PARTICIPANT)
            .build();
    participantRepository.save(participant);

    if (challenge.getGoalType().equals(GoalType.FLEXIBLE)) {
      if (goals == null || goals.isEmpty()) {
        throw new CustomException(ErrorCode.FLEXIBLE_GOAL_REQUIRED);
      }
      for (String g : goals) {
        challengeGoalRepository.save(
            ChallengeGoal.builder().content(g).participant(participant).build());
      }
    } else {
      // 고정목표: 챌린지의 원본 목표(fixed_challenge_goal)를 참여자의 challenge_goal 로 복제한다.
      for (FixedChallengeGoal fg : fixedChallengeGoalRepository.findByChallengeId(challengeId)) {
        challengeGoalRepository.save(
            ChallengeGoal.builder().participant(participant).content(fg.getContent()).build());
      }
    }
    entityManager.flush();
    entityManager.refresh(participant);
    return toChallengeResponse(challenge, member);
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
    } else if (challenge.getMaxParticipantsCnt() != null
        && getParticipantCnt(challenge.getId()) >= challenge.getMaxParticipantsCnt()) {
      throw new CustomException(ErrorCode.CANNOT_ACCEPT_PARTICIPANT);
    }
    participant.setStatus(ParticipantStatus.PARTICIPANT);
    participantRepository.save(participant);
    notificationService.notifyChallengeApproved(
        memberId, participant.getMember().getId(), challenge.getId(), challenge.getTitle());
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
          memberId,
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
    // "오늘 시작해볼 챌린지": 비공개/삭제/종료 챌린지는 제외하고 진행중·예정만 노출한다.
    // (종료 판정 = endDate < today. 무기한(endDate==null)은 종료가 아니므로 포함.)
    LocalDate today = LocalDate.now();
    List<Challenge> all =
        challengeRepository.findAll().stream()
            .filter(c -> c.getChallengeType() != ChallengeType.PRIVATE)
            .filter(c -> c.getDeletedAt() == null)
            .filter(c -> c.getEndDate() == null || !c.getEndDate().isBefore(today))
            .collect(java.util.stream.Collectors.toList());
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
      total += diaryGoalRepository.countByChallengeGoalIdAndDiary_IsDeletedFalse(cg.getId());
    }
    return total;
  }

  public Pagination<ChallengeSummaryResponse> getChallengeList(
      Long memberId,
      int limit,
      String cursor,
      String keyword,
      List<Category> categories,
      ChallengeType challengeType,
      List<ChallengeStatus> statuses) {
    String kw = (keyword == null) ? "" : keyword.trim();
    Long cursorId =
        (cursor == null || cursor.isBlank()) ? null : cursorService.decodeCursorToId(cursor);

    Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "id"));
    List<Challenge> rows =
        challengeRepository.searchPage(
            cursorId,
            kw,
            ChallengeType.PRIVATE.name(),
            challengeType != null ? challengeType.name() : null,
            isAllCategory(categories),
            toCategoryNames(categories),
            isAllStatus(statuses),
            toStatusNames(statuses),
            LocalDate.now(),
            pageable);

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

  @Transactional
  public ChallengePokeResponse pokeChallengeMembers(
      Long challengeId, Long actorId, ChallengePokeRequest request) {
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    Member actor =
        memberRepository
            .findById(actorId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    if (!isActiveParticipant(challengeId, actorId)) {
      throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND);
    }
    if (request == null
        || request.getReceiverMemberIds() == null
        || request.getReceiverMemberIds().isEmpty()) {
      throw new CustomException(ErrorCode.CHALLENGE_POKE_EMPTY_TARGET);
    }

    LocalDate today = LocalDate.now();
    Set<Long> receiverIds = new LinkedHashSet<>(request.getReceiverMemberIds());
    List<ChallengePoke> pokes = new ArrayList<>();

    for (Long receiverId : receiverIds) {
      if (receiverId == null) {
        throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
      }
      if (receiverId.equals(actorId)) {
        throw new CustomException(ErrorCode.CHALLENGE_POKE_SELF);
      }
      if (!isActiveParticipant(challengeId, receiverId)) {
        throw new CustomException(ErrorCode.CHALLENGE_POKE_TARGET_NOT_PARTICIPANT);
      }
      if (diaryRepository.existsByChallengeIdAndMemberIdAndCompletedDateAndIsDeletedFalse(
          challengeId, receiverId, today)) {
        throw new CustomException(ErrorCode.CHALLENGE_POKE_TARGET_ALREADY_WRITTEN);
      }
      if (challengePokeRepository.existsByChallengeIdAndActorIdAndReceiverIdAndPokedDate(
          challengeId, actorId, receiverId, today)) {
        throw new CustomException(ErrorCode.CHALLENGE_POKE_ALREADY_SENT);
      }

      Member receiver =
          memberRepository
              .findById(receiverId)
              .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      pokes.add(
          ChallengePoke.builder()
              .challenge(challenge)
              .actor(actor)
              .receiver(receiver)
              .pokedDate(today)
              .build());
    }

    challengePokeRepository.saveAll(pokes);

    for (ChallengePoke poke : pokes) {
      notificationService.notifyChallengePoke(
          actorId,
          poke.getReceiver().getId(),
          challengeId,
          challenge.getTitle(),
          actor.getNickname());
    }

    return new ChallengePokeResponse(receiverIds.stream().toList());
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

  private List<MemberInfo> pickRandomParticipants(Long challengeId, int size) {
    List<Participant> participants =
        participantRepository.findByChallengeIdAndStatusIn(
            challengeId, List.of(ParticipantStatus.HOST, ParticipantStatus.PARTICIPANT));

    if (participants.isEmpty()) return List.of();

    Collections.shuffle(participants);
    return participants.stream()
        .limit(size)
        .map(
            p ->
                MemberInfo.from(
                    p.getMember(), imageService.getFileUrl(p.getMember().getProfileUrl())))
        .toList();
  }

  public ChallengeResponse toChallengeResponse(Challenge challenge, Member member) {
    Long challengeId = challenge.getId();
    Long memberId = member.getId();
    // 챌린지 목표
    // 참여자(HOST/PARTICIPANT)는 본인의 challenge_goal(일지와 연결되는 목표)을 보고,
    // 참여 전(NONE 등)에는 고정목표 챌린지의 원본 목표(fixed_challenge_goal)를 미리보기로 노출한다.
    List<ChallengeGoalDto> challengeGoals;
    ParticipantStatus status = getMemberStatus(challengeId, memberId);
    if ((status == ParticipantStatus.HOST) || (status == ParticipantStatus.PARTICIPANT)) {
      challengeGoals =
          participantRepository
              .findByMemberIdAndChallengeId(memberId, challengeId)
              .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND))
              .getChallengeGoals()
              .stream()
              .map(this::toChallengeGoal)
              .toList();
    } else if (challenge.getGoalType() == GoalType.FIXED) {
      challengeGoals =
          fixedChallengeGoalRepository.findByChallengeId(challengeId).stream()
              .map(this::toChallengeGoal)
              .toList();
    } else {
      challengeGoals = Collections.emptyList();
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
    // 챌린지 상세에는 등수순 상위 5명만 노출한다(전체는 GET /challenges/{id}/participants 로 페이징 조회).
    Map<Long, ChallengeRankingService.RankInfo> ranks =
        challengeRankingService.computeRanks(challengeId, participants);
    List<ParticipantResponse> topParticipants =
        participants.stream()
            .sorted(
                Comparator.comparingInt((Participant p) -> ranks.get(p.getId()).rank())
                    .thenComparing(Participant::getId))
            .limit(5)
            .map(p -> toParticipant(p, ranks.get(p.getId())))
            .toList();
    return new ChallengeResponse(
        toChallengeSummary(challenge, memberId),
        toChallengeDetail(challenge, memberId),
        challengeGoals,
        topParticipants);
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
        challenge.getChallengeType(),
        getParticipantCnt(challengeId),
        likeInfo,
        challenge.getDeletedAt() != null,
        pickRandomParticipants(challengeId, 3),
        challenge.isPhotoRequired(),
        challenge.isPostEndWriteAllowed());
  }

  private ChallengeDetailDto toChallengeDetail(Challenge challenge, Long memberId) {
    return new ChallengeDetailDto(
        challenge.getDescription(),
        challenge.isAllowMidJoin(),
        challenge.isPhotoRequired(),
        challenge.isPostEndWriteAllowed(),
        getMemberStatus(challenge.getId(), memberId),
        getParticipationRate(challenge),
        getGoalCompletionRate(challenge));
  }

  private final ChallengeGoalDto toChallengeGoal(ChallengeGoal challengeGoal) {
    return new ChallengeGoalDto(challengeGoal.getId(), challengeGoal.getContent());
  }

  private final ChallengeGoalDto toChallengeGoal(FixedChallengeGoal fixedChallengeGoal) {
    return new ChallengeGoalDto(fixedChallengeGoal.getId(), fixedChallengeGoal.getContent());
  }

  private final ParticipantResponse toParticipant(Participant participant) {
    return toParticipant(participant, null);
  }

  private ParticipantResponse toParticipant(
      Participant participant, ChallengeRankingService.RankInfo rankInfo) {
    Member member = participant.getMember();
    String profileUrl =
        member.getProfileUrl() == null ? "" : imageService.getFileUrl(member.getProfileUrl());
    List<ChallengeGoalDto> goals =
        participant.getChallengeGoals().stream().map(this::toChallengeGoal).toList();
    return ParticipantResponse.builder()
        .memberId(member.getId())
        .participantId(participant.getId())
        .nickname(member.getNickname())
        .profileImg(profileUrl)
        .status(participant.getStatus())
        .goals(goals)
        .rank(rankInfo != null ? rankInfo.rank() : null)
        .streak(rankInfo != null ? rankInfo.streak() : null)
        .completedGoalCount(rankInfo != null ? (int) rankInfo.completedGoalCount() : null)
        .build();
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

  private boolean isActiveParticipant(Long challengeId, Long memberId) {
    return participantRepository.existsByChallengeIdAndMemberIdAndStatus(
            challengeId, memberId, ParticipantStatus.HOST)
        || participantRepository.existsByChallengeIdAndMemberIdAndStatus(
            challengeId, memberId, ParticipantStatus.PARTICIPANT);
  }

  private double getParticipationRate(Challenge challenge) {
    LocalDate startDate = challenge.getStartDate();
    LocalDate endDate = challenge.getEndDate();
    LocalDate today = LocalDate.now();
    if (startDate == null || today.isBefore(startDate)) return -1; // 시작일 없음/시작 전

    Long challengeId = challenge.getId();
    long allGoalsCompletedDiaryCnt =
        diaryRepository.countByChallengeIdAndIsAllGoalsCompletedTrueAndIsDeletedFalse(challengeId);

    long participantCnt = getParticipantCnt(challengeId);
    if (participantCnt == 0) return 0;

    // 경과일: 무기한(endDate==null)이거나 아직 종료 전이면 오늘까지, 종료됐으면 종료일까지. (endDate null 시 NPE 방지)
    LocalDate effectiveEnd = (endDate == null || today.isBefore(endDate)) ? today : endDate;
    long days = ChronoUnit.DAYS.between(startDate, effectiveEnd) + 1;
    if (days <= 0) return 0;

    return (double) allGoalsCompletedDiaryCnt / ((double) participantCnt * (double) days) * 100;
  }

  private double getGoalCompletionRate(Challenge challenge) {
    if (challenge.getGoalType() == GoalType.FLEXIBLE) return -1;
    LocalDate startDate = challenge.getStartDate();
    LocalDate endDate = challenge.getEndDate();
    LocalDate today = LocalDate.now();
    if (startDate == null || today.isBefore(startDate)) return -1; // 시작일 없음/시작 전

    Long challengeId = challenge.getId();

    // 수행된 목표 수
    long completedGoalCnt =
        diaryGoalRepository.countByDiary_Challenge_IdAndIsCompletedTrueAndDiary_IsDeletedFalse(
            challengeId);
    // 전체 참여자
    long participantCnt = getParticipantCnt(challengeId);
    if (participantCnt == 0) return 0; // 0 나눗셈(NaN) 방지
    // 진행된 일 수: 무기한/종료 전이면 오늘까지, 종료됐으면 종료일까지. (endDate null 시 NPE 방지)
    LocalDate effectiveEnd = (endDate == null || today.isBefore(endDate)) ? today : endDate;
    long days = ChronoUnit.DAYS.between(startDate, effectiveEnd) + 1;
    if (days <= 0) return 0;
    // 목표의 개수 (고정목표 챌린지의 원본 목표 기준)
    long goalCnt = fixedChallengeGoalRepository.countByChallengeId(challengeId);
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
