package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.comment.repository.CommentRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.entity.*;
import com.odos.odos_server_v2.domain.diary.repository.*;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.domain.shared.dto.PageInfo;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.io.IOException;
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
  private final ChallengeGoalRepository challengeGoalRepository;
  private final CommentRepository commentRepository;

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

    // 챌린지 타입별 목표를 기반으로 다이어리 목표달성 생성 및 저장
    List<ChallengeGoal> challengeGoals;
    if (challenge.getType().equals(ChallengeType.FIXED)) {
      challengeGoals =
          challengeGoalRepository.getFixedGoals(
              challenge.getHostMember().getId(), challenge.getId());
    } else {
      challengeGoals = participant.getChallengeGoals();
    }
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
            .findById(diaryId)
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

    List<ChallengeGoal> challengeGoals;
    if (challenge.getType().equals(ChallengeType.FIXED)) {
      challengeGoals =
          challengeGoalRepository.getFixedGoals(
              challenge.getHostMember().getId(), challenge.getId());
    } else {
      challengeGoals = participant.getChallengeGoals();
    }

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
            .findById(diaryId)
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
    List<Diary> diaries = diaryRepository.findDiariesByIsPublic(Boolean.TRUE);
    List<DiaryResponse> diaryResponses = new ArrayList<>();

    for (Diary diary : diaries) {
      diaryResponses.add(
          DiaryResponse.from(
              member,
              diary,
              challengeService.toChallengeSummary(diary.getChallenge(), memberId),
              imageService.getFileUrl(diary.getMember().getProfileUrl()),
              commentRepository.countByDiaryId(diary.getId())));
    }
    return diaryResponses;
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

    List<DiaryResponse> items = new ArrayList<>(diaries.size());
    for (Diary diary : diaries) {
      items.add(
          DiaryResponse.from(
              member,
              diary,
              challengeService.toChallengeSummary(diary.getChallenge(), memberId),
              imageService.getFileUrl(diary.getMember().getProfileUrl()),
              commentRepository.countByDiaryId(diary.getId())));
    }

    String nextCursor = null;
    if (hasNext && !diaries.isEmpty()) {
      Long lastId = diaries.get(diaries.size() - 1).getId();
      nextCursor = cursorService.encodeCursor(lastId);
    }

    PageInfo pageInfo = new PageInfo((long) limit, hasNext, nextCursor);
    return new Pagination<>(items, pageInfo);
  }

  public Boolean deleteDiary(Long diaryId) {
    diaryRepository
        .findById(diaryId)
        .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    diaryRepository.deleteById(diaryId);
    return true;
  }

  @Transactional
  public Integer addDiaryLike(Long memberId, Long diaryId) {
    Diary diary =
        diaryRepository
            .findById(diaryId)
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
      return likes.size();
    } else {
      throw new CustomException(ErrorCode.DIARYLIKE_ALREADY_EXISTS);
    }
  }

  public Integer cancelDiaryLike(Long memberId, Long diaryId) {
    diaryRepository
        .findById(diaryId)
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

      List<Diary> diaries = diaryRepository.findDiariesByIsPublic(Boolean.TRUE);
      if (diaries.isEmpty()) {
        return Collections.emptyList();
      }

      Collections.shuffle(diaries);
      return diaries.stream()
          .limit(size)
          .map(
              diary ->
                  DiaryResponse.from(
                      currentMember,
                      diary,
                      challengeService.toChallengeSummary(diary.getChallenge(), currentMemberId),
                      imageService.getFileUrl(diary.getMember().getProfileUrl()),
                      commentRepository.countByDiaryId(diary.getId())))
          .toList();
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
            .findById(request.getDiaryId())
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
    Page<Diary> diaries = diaryRepository.findDiariesByMember_Id(memberId, pageable);

    Page<DiaryResponse> diaryResponsePage =
        diaries.map(
            diary ->
                DiaryResponse.from(
                    member,
                    diary,
                    challengeService.toChallengeSummary(diary.getChallenge(), memberId),
                    imageService.getFileUrl(diary.getMember().getProfileUrl()),
                    commentRepository.countByDiaryId(diary.getId())));

    return OffsetPagination.from(diaryResponsePage);
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
            .findById(diaryId)
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
      diaries = diaryRepository.findAllByChallengeId(challengeId, pageable);
    } else {
      diaries =
          diaryRepository.findDiariesByChallengeIdAndIsPublic(challengeId, Boolean.TRUE, pageable);
    }

    Page<DiaryResponse> result =
        diaries.map(
            diary ->
                DiaryResponse.from(
                    member,
                    diary,
                    summary,
                    imageService.getFileUrl(diary.getMember().getProfileUrl()),
                    commentRepository.countByDiaryId(diary.getId())));
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
      List<DiaryResponse> diaryResponses = new ArrayList<>();
      for (Diary diary : diaries) {
        diaryResponses.add(
            DiaryResponse.from(
                member,
                diary,
                challengeService.toChallengeSummary(diary.getChallenge(), memberId),
                imageService.getFileUrl(diary.getMember().getProfileUrl()),
                commentRepository.countByDiaryId(diary.getId())));
      }
      return diaryResponses;
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

    Page<DiaryResponse> diaryResponsePage =
        diaries.map(
            diary ->
                DiaryResponse.from(
                    member,
                    diary,
                    challengeService.toChallengeSummary(diary.getChallenge(), memberId),
                    imageService.getFileUrl(diary.getMember().getProfileUrl()),
                    commentRepository.countByDiaryId(diary.getId())));

    return OffsetPagination.from(diaryResponsePage);
  }
}
