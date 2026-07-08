package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.comment.repository.CommentRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.entity.*;
import com.odos.odos_server_v2.domain.diary.repository.*;
import com.odos.odos_server_v2.domain.friend.repository.FriendRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.notification.service.NotificationService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.dto.PageInfo;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
  private final DiaryLikeRepository diaryLikeRepository;
  private final DiaryReportRepository diaryReportRepository;
  private final ChallengeRepository challengeRepository;
  private final DiaryGoalRepository diaryGoalRepository;
  private final ChallengeService challengeService;
  private final CursorService cursorService;
  private final ParticipantRepository participantRepository;
  private final ImageService imageService;
  private final DiaryImageRepository diaryImageRepository;
  private final CommentRepository commentRepository;
  private final NotificationService notificationService;
  private final FriendRepository friendRepository;

  @Transactional
  public DiaryResponse createDiary(Long memberId, DiaryRequest request) {

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Challenge challenge =
        challengeRepository
            .findById(request.getChallengeId())
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    ChallengeSummaryResponse challengeSummary =
        challengeService.toChallengeSummary(challenge, memberId);

    Participant participant =
        participantRepository
            .findByMemberIdAndChallengeId(memberId, challenge.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));

    Boolean isCheckedAll = false;
    Diary diary =
        Diary.builder()
            .member(member)
            .challenge(challenge)
            .completedDate(request.getAchievedDate())
            .title(request.getTitle())
            .content(request.getContent())
            .feeling(request.getFeeling())
            .isPublic(request.getIsPublic())
            .isAllGoalsCompleted(isCheckedAll)
            .diaryGoals(new ArrayList<>())
            .likes(new ArrayList<>())
            .build();
    Diary newDiary = diaryRepository.save(diary);

    // presigned로 업로드된 이미지 URL을 세팅 (cascade로 함께 저장됨)
    validateImageUrls(request.getImageUrls());
    if (request.getImageUrls() != null) {
      newDiary.replaceImages(request.getImageUrls());
      newDiary.updateThumbnailUrl(
          resolveThumbnailUrl(request.getImageUrls(), request.getThumbnailUrl()));
    }

    // 챌린지 타입별 목표를 기반으로 다이어리 목표달성 생성 및 저장
    // FIXED/FLEXIBLE 모두 참여자 본인의 challenge_goal 을 기준으로 일지 목표를 생성한다.
    // (고정목표 챌린지도 참여 시 fixed_challenge_goal 이 참여자의 challenge_goal 로 복제되어 있다.)
    List<ChallengeGoal> challengeGoals = participant.getChallengeGoals();
    List<DiaryGoal> diaryGoals = new ArrayList<>();
    List<Long> achievedGoalIds =
        request.getAchievedGoalIds() != null ? request.getAchievedGoalIds() : new ArrayList<>();

    Set<Long> achievedGoalIdSet =
        new HashSet<>(
            request.getAchievedGoalIds() != null
                ? request.getAchievedGoalIds()
                : new ArrayList<>());

    boolean isAllGoalsCompleted =
        challengeGoals.stream().allMatch(goal -> achievedGoalIdSet.contains(goal.getId()));
    newDiary.updateIsAllGoalsCompleted(isAllGoalsCompleted);

    Set<Long> challengeGoalIdSet =
        challengeGoals.stream().map(ChallengeGoal::getId).collect(Collectors.toSet());

    for (Long id : achievedGoalIds) {
      if (!challengeGoalIdSet.contains(id)) {
        throw new CustomException(ErrorCode.DIARY_NOT_CREATED);
      }
    }

    for (ChallengeGoal challengeGoal : challengeGoals) {
      boolean isCompleted = achievedGoalIds.contains(challengeGoal.getId());
      DiaryGoal diaryGoal =
          DiaryGoal.builder()
              .diary(newDiary)
              .challengeGoal(challengeGoal)
              .isCompleted(isCompleted)
              .build();
      diaryGoals.add(diaryGoal);
      newDiary.addDiaryGoal(diaryGoal);
    }
    diaryGoalRepository.saveAll(diaryGoals);

    List<Long> friendIds =
        friendRepository.findByMember(member).stream()
            .map(friend -> friend.getFriendMember().getId())
            .toList();

    notificationService.notifyFriendDiaryCreated(
        member.getId(), friendIds, newDiary.getId(), member.getNickname(), newDiary.getTitle());

    return DiaryResponse.from(
        member,
        newDiary,
        challengeSummary,
        imageService.getFileUrl(newDiary.getMember().getProfileUrl()),
        0);
  }

  @Transactional
  public DiaryResponse updateDiary(Long memberId, Long diaryId, DiaryRequest request) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

    if (!diary.getMember().getId().equals(memberId)) {
      throw new CustomException(ErrorCode.DIARY_NOT_ACCESS);
    }

    Challenge challenge =
        challengeRepository
            .findById(request.getChallengeId())
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    ChallengeSummaryResponse challengeSummary =
        challengeService.toChallengeSummary(challenge, memberId);

    Participant participant =
        participantRepository
            .findByMemberIdAndChallengeId(memberId, challenge.getId())
            .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND));

    // FIXED/FLEXIBLE 모두 참여자 본인의 challenge_goal 을 기준으로 일지 목표를 생성한다.
    // (고정목표 챌린지도 참여 시 fixed_challenge_goal 이 참여자의 challenge_goal 로 복제되어 있다.)
    List<ChallengeGoal> challengeGoals = participant.getChallengeGoals();

    List<DiaryGoal> diaryGoals = new ArrayList<>();
    List<Long> achievedGoalIds =
        request.getAchievedGoalIds() != null ? request.getAchievedGoalIds() : new ArrayList<>();

    Set<Long> achievedGoalIdSet =
        new HashSet<>(
            request.getAchievedGoalIds() != null
                ? request.getAchievedGoalIds()
                : new ArrayList<>());

    boolean isAllGoalsCompleted =
        challengeGoals.stream().allMatch(goal -> achievedGoalIdSet.contains(goal.getId()));
    diary.updateIsAllGoalsCompleted(isAllGoalsCompleted);

    Set<Long> challengeGoalIdSet =
        challengeGoals.stream().map(ChallengeGoal::getId).collect(Collectors.toSet());

    for (Long id : achievedGoalIds) {
      if (!challengeGoalIdSet.contains(id)) {
        throw new CustomException(ErrorCode.DIARY_NOT_CREATED);
      }
    }

    for (ChallengeGoal challengeGoal : challengeGoals) {
      boolean isCompleted = achievedGoalIds.contains(challengeGoal.getId());
      DiaryGoal diaryGoal =
          DiaryGoal.builder()
              .diary(diary) // newDiary로
              .challengeGoal(challengeGoal)
              .isCompleted(isCompleted)
              .build();
      diaryGoals.add(diaryGoal);
    }

    diary.updateDiary(request, challenge, diaryGoals);

    // 이미지 전체 교체(clear-and-replace): 기존 DiaryImage 제거 후 imageUrls로 재구성
    // imageUrls가 null이면 이미지/썸네일 모두 그대로 유지, 값이 있으면 썸네일도 새 배열 기준으로 재설정
    validateImageUrls(request.getImageUrls());
    if (request.getImageUrls() != null) {
      diary.replaceImages(request.getImageUrls());
      diary.updateThumbnailUrl(
          resolveThumbnailUrl(request.getImageUrls(), request.getThumbnailUrl()));
    }

    diaryRepository.save(diary);
    return DiaryResponse.from(
        member,
        diary,
        challengeSummary,
        imageService.getFileUrl(diary.getMember().getProfileUrl()),
        commentRepository.countByDiaryId(diary.getId()));
  }

  @Transactional
  public DiaryResponse getDiary(Long diaryId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member viewer =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

    Long writer = diary.getMember().getId();

    // 비공개 일지 만족 -> 작성자가 아닐 시 접근불허 (애초에 안 보이게 했지만 혹시나하는)
    if (!diary.getIsPublic()) {
      if (!memberId.equals(writer)) {
        throw new CustomException(ErrorCode.DIARY_NOT_ACCESS);
      }
    }

    ChallengeSummaryResponse response =
        challengeService.toChallengeSummary(diary.getChallenge(), memberId);

    return DiaryResponse.from(
        viewer,
        diary,
        response,
        imageService.getFileUrl(diary.getMember().getProfileUrl()),
        commentRepository.countByDiaryId(diary.getId()));
  }

  @Transactional
  public List<DiaryResponse> getAllPublicDiaries() {
    Long memberId = CurrentUserContext.getCurrentMemberIdOrNull();
    Member member = (memberId != null) ? memberRepository.findById(memberId).orElse(null) : null;
    List<Diary> diaries = diaryRepository.findDiariesByIsPublicAndIsDeletedFalse(Boolean.TRUE);
    return toDiaryResponses(member, memberId, diaries);
  }

  @Transactional
  public Pagination<DiaryResponse> getPublicDiariesPage(Integer size, String cursor) {
    int limit = (size == null || size <= 0) ? 10 : Math.min(size, 100);

    Long memberId = CurrentUserContext.getCurrentMemberIdOrNull();
    Member member = (memberId != null) ? memberRepository.findById(memberId).orElse(null) : null;

    Long cursorId = cursorService.decodeCursorToId(cursor);

    List<Diary> diaries =
        diaryRepository.findPublicPage(
            cursorId, PageRequest.of(0, limit + 1)); // hasNext 판별 위해 limit + 1로 조회

    boolean hasNext = diaries.size() > limit;
    if (hasNext) {
      diaries = diaries.subList(0, limit);
    }

    List<DiaryResponse> items = toDiaryResponses(member, memberId, diaries);

    String nextCursor = null;
    if (hasNext && !diaries.isEmpty()) {
      Long lastId = diaries.get(diaries.size() - 1).getId();
      nextCursor = cursorService.encodeCursor(lastId);
    }

    PageInfo pageInfo = new PageInfo((long) limit, hasNext, nextCursor);
    return new Pagination<>(items, pageInfo);
  }

  @Transactional
  public Boolean deleteDiary(Long diaryId) {
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    // diaryRepository.deleteById(diaryId);
    diary.softDelete();
    diaryRepository.save(diary);
    return true;
  }

  @Transactional
  public Integer addDiaryLike(Long memberId, Long diaryId) {
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    Member pressedMember =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Optional<DiaryLike> like =
        diaryLikeRepository.findDiaryLikeByDiaryIdAndMemberId(diaryId, memberId);
    if (like.isEmpty()) {
      DiaryLike diaryLike = DiaryLike.builder().diary(diary).member(pressedMember).build();
      diaryLike.setDiary(diary);
      diaryLikeRepository.save(diaryLike);
      List<DiaryLike> likes = diaryLikeRepository.getDiaryLikeCountByDiaryId(diaryId);
      int likeCount = likes.size();
      notificationService.notifyDiaryLikeMilestone(diaryId, likeCount);
      return likeCount;
    } else {
      throw new CustomException(ErrorCode.DIARYLIKE_ALREADY_EXISTS);
    }
  }

  public Integer cancelDiaryLike(Long memberId, Long diaryId) {
    diaryRepository
        .findByIdAndIsDeletedFalse(diaryId)
        .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Optional<DiaryLike> like =
        diaryLikeRepository.findDiaryLikeByDiaryIdAndMemberId(diaryId, memberId);
    if (like.isEmpty()) {
      throw new CustomException(ErrorCode.DIARYLIKE_NOT_EXISTS);
    } else {
      diaryLikeRepository.delete(like.get());
      return diaryLikeRepository.getDiaryLikeCountByDiaryId(diaryId).size();
    }
  }

  @Transactional
  public List<DiaryResponse> getRandomDiaries(Long size) {
    try {
      Long currentMemberId = CurrentUserContext.getCurrentMemberIdOrNull();

      Member currentMember =
          (currentMemberId != null)
              ? memberRepository.findById(currentMemberId).orElse(null)
              : null;

      List<Diary> diaries = diaryRepository.findDiariesByIsPublicAndIsDeletedFalse(Boolean.TRUE);
      if (diaries.isEmpty()) {
        return Collections.emptyList();
      }

      Collections.shuffle(diaries);
      List<Diary> selectedDiaries = diaries.stream().limit(size).toList();
      return toDiaryResponses(currentMember, currentMemberId, selectedDiaries);
    } catch (CustomException e) {
      return Collections.emptyList();
    }
  }

  @Transactional
  public Boolean reportDiary(ReportRequest request, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(request.getDiaryId())
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    DiaryReport diaryReport =
        DiaryReport.builder()
            .member(member)
            .diary(diary)
            .type(request.getReportType())
            .content(request.getContent())
            .build();
    diaryReportRepository.save(diaryReport);
    return true;
  }

  // 마이페이지 다이어리 조회를 위한 서비스 메서드
  @Transactional
  public OffsetPagination<DiaryResponse> getMyDiaries(Pageable pageable) {

    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Page<Diary> diaries =
        diaryRepository.findDiariesByMember_IdAndIsDeletedFalse(
            memberId, pageable); // TODO : 마이페이지에서도 softDelete된거 안보이게? 적용하긴 하였으나

    Page<DiaryResponse> diaryResponsePage = toDiaryResponsePage(member, memberId, diaries);

    return OffsetPagination.from(diaryResponsePage);
  }

  // imageUrls는 반드시 우리 스토리지에서 발급한 presigned fileUrl이어야 한다. (임의 외부 URL 저장 방지)
  private void validateImageUrls(List<String> imageUrls) {
    if (imageUrls == null || imageUrls.isEmpty()) {
      return;
    }
    String allowedPrefix = imageService.getFileUrl("");
    for (String url : imageUrls) {
      if (url == null || !url.startsWith(allowedPrefix)) {
        throw new CustomException(ErrorCode.DIARY_INVALID_IMAGE_URL);
      }
    }
  }

  // 대표 썸네일 결정. imageUrls는 non-null 가정(호출부에서 보장), 요소는 이미 validateImageUrls 통과.
  // - thumbnailUrl 지정 시 imageUrls에 포함돼야 함(아니면 DIARY-009, 빈 배열도 여기서 걸림)
  // - thumbnailUrl null 이면 imageUrls 첫 요소, 빈 배열이면 null
  private String resolveThumbnailUrl(List<String> imageUrls, String thumbnailUrl) {
    if (thumbnailUrl == null) {
      return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }
    if (!imageUrls.contains(thumbnailUrl)) {
      throw new CustomException(ErrorCode.DIARY_INVALID_THUMBNAIL_URL);
    }
    return thumbnailUrl;
  }

  @Transactional
  public String uploadDiaryFile(Long diaryId, MultipartFile file) throws IOException {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    String fileName = imageService.uploadFile(file);
    DiaryImage diaryImage = DiaryImage.builder().diary(diary).url(fileName).build();
    diary.addDiaryImage(diaryImage);
    diaryImageRepository.save(diaryImage);
    return fileName;
  }

  @Transactional
  public List<String> uploadDiaryFiles(Long diaryId, List<MultipartFile> files) throws IOException {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Diary diary =
        diaryRepository
            .findByIdAndIsDeletedFalse(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    List<String> fileList = imageService.uploadFiles(files);
    List<DiaryImage> diaryImages = new ArrayList<>();
    for (String fileName : fileList) {
      diaryImages.add(DiaryImage.builder().diary(diary).url(fileName).build());
    }
    diaryImageRepository.saveAll(diaryImages);
    return fileList;
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getChallengeDiaries(Long challengeId, Pageable pageable) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    ChallengeSummaryResponse summary = challengeService.toChallengeSummary(challenge, memberId);
    Page<Diary> diaries = null;
    if ((participantRepository.existsByChallengeIdAndMemberIdAndStatus(
            challengeId, memberId, ParticipantStatus.PARTICIPANT)
        || (participantRepository.existsByChallengeIdAndMemberIdAndStatus(
            challengeId, memberId, ParticipantStatus.HOST)))) {
      diaries = diaryRepository.findAllByChallengeIdAndIsDeletedFalse(challengeId, pageable);
    } else {
      diaries =
          diaryRepository.findDiariesByChallengeIdAndIsPublicAndIsDeletedFalse(
              challengeId, Boolean.TRUE, pageable);
    }

    Map<Long, Long> commentCounts = getCommentCountMap(diaries.getContent());
    Page<DiaryResponse> result =
        diaries.map(
            diary ->
                DiaryResponse.from(
                    member,
                    diary,
                    summary,
                    imageService.getFileUrl(diary.getMember().getProfileUrl()),
                    commentCounts.getOrDefault(diary.getId(), 0L)));
    return OffsetPagination.from(result);
  }

  // 다른 사람 프로필 조회 시 공개 다이어리만 조회
  @Transactional
  public List<DiaryResponse> getOtherPublicDiaries(Long otherMemberId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    try {
      Member member =
          memberRepository
              .findById(memberId)
              .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      List<Diary> diaries = diaryRepository.findOthersPublicDiaries(otherMemberId);
      return toDiaryResponses(member, memberId, diaries);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getOtherPublicDiariesByOffset(
      Long otherMemberId, Pageable pageable) {
    Long memberId = CurrentUserContext.getCurrentMemberId();

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Page<Diary> diaries = diaryRepository.findOthersPublicDiariesByOffset(otherMemberId, pageable);

    Page<DiaryResponse> diaryResponsePage = toDiaryResponsePage(member, memberId, diaries);

    return OffsetPagination.from(diaryResponsePage);
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getDiariesByCompletedDate(
      LocalDate completedDate, Pageable pageable) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Page<Diary> diaries = diaryRepository.findDiariesWithCompletedDate(completedDate, pageable);

    Page<DiaryResponse> diaryResponsePage = toDiaryResponsePage(member, memberId, diaries);

    return OffsetPagination.from(diaryResponsePage);
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getDiariesByCreatedDate(
      LocalDate createdAt, Pageable pageable) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Page<Diary> diaries = diaryRepository.findDiariesWithCreatedDate(createdAt, pageable);

    Page<DiaryResponse> diaryResponsePage = toDiaryResponsePage(member, memberId, diaries);

    return OffsetPagination.from(diaryResponsePage);
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getDiariesByCompletedDateWithRange(
      LocalDate start, LocalDate last, Pageable pageable) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Page<Diary> diaries =
        diaryRepository.findDiariesByDateRangeWithCompletedDate(start, last, pageable);

    Page<DiaryResponse> diaryResponsePage = toDiaryResponsePage(member, memberId, diaries);

    return OffsetPagination.from(diaryResponsePage);
  }

  @Transactional
  public OffsetPagination<DiaryResponse> getDiariesByCreatedDateWithRange(
      LocalDate start, LocalDate last, Pageable pageable) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Page<Diary> diaries =
        diaryRepository.findDiariesByDateRangeWithCreatedDate(start, last, pageable);

    Page<DiaryResponse> diaryResponsePage = toDiaryResponsePage(member, memberId, diaries);

    return OffsetPagination.from(diaryResponsePage);
  }

  private List<DiaryResponse> toDiaryResponses(Member member, Long memberId, List<Diary> diaries) {
    if (diaries == null || diaries.isEmpty()) {
      return Collections.emptyList();
    }

    Map<Long, Long> commentCounts = getCommentCountMap(diaries);

    return diaries.stream()
        .map(diary -> toDiaryResponse(member, memberId, diary, commentCounts))
        .toList();
  }

  private Page<DiaryResponse> toDiaryResponsePage(
      Member member, Long memberId, Page<Diary> diaries) {
    Map<Long, Long> commentCounts = getCommentCountMap(diaries.getContent());

    return diaries.map(diary -> toDiaryResponse(member, memberId, diary, commentCounts));
  }

  private DiaryResponse toDiaryResponse(
      Member member, Long memberId, Diary diary, Map<Long, Long> commentCounts) {
    return DiaryResponse.from(
        member,
        diary,
        challengeService.toChallengeSummary(diary.getChallenge(), memberId),
        imageService.getFileUrl(diary.getMember().getProfileUrl()),
        commentCounts.getOrDefault(diary.getId(), 0L));
  }

  private Map<Long, Long> getCommentCountMap(List<Diary> diaries) {
    if (diaries == null || diaries.isEmpty()) {
      return Collections.emptyMap();
    }

    List<Long> diaryIds = diaries.stream().map(Diary::getId).filter(Objects::nonNull).toList();
    if (diaryIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<Long, Long> commentCounts = new HashMap<>();
    for (Object[] row : diaryRepository.countCommentsByDiaryIds(diaryIds)) {
      commentCounts.put((Long) row[0], (Long) row[1]);
    }
    return commentCounts;
  }

  @Transactional
  public void softDeleteWithdrawnMemberDiaries(Long memberId) {
    if (memberId == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    List<Diary> diariesByMemberId = diaryRepository.findDiariesByMember_Id(memberId);
    if (diariesByMemberId == null || diariesByMemberId.isEmpty()) {
      return;
    }

    for (Diary diary : diariesByMemberId) {
      try {
        if (diary == null || Boolean.TRUE.equals(diary.getIsDeleted())) {
          continue;
        }
        diary.softDelete();
      } catch (Exception e) {
        log.warn(
            "Failed to soft delete diary. memberId={}, diaryId={}", memberId, diary.getId(), e);
      }
    }
  }
}
