package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.entity.*;
import com.odos.odos_server_v2.domain.diary.repository.*;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.dto.PageInfo;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.domain.shared.service.CursorService;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
        participantRepository.findByMemberIdAndChallengeId(memberId, challenge.getId());
    if (participant == null) {
      throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND);
    }

    Diary diary =
        Diary.builder()
            .member(member)
            .challenge(challenge)
            .completedDate(request.getAchievedDate())
            .title(request.getTitle())
            .content(request.getContent())
            .feeling(request.getFeeling())
            .isPublic(request.getIsPublic())
            .diaryGoals(new ArrayList<>())
            .likes(new ArrayList<>())
            .build();
    Diary newDiary = diaryRepository.save(diary);

    // 챌린지 목표를 기반으로 다이어리 목표달성 생성 및 저장
    List<ChallengeGoal> challengeGoals = Objects.requireNonNull(participant).getChallengeGoals();
    List<DiaryGoal> diaryGoals = new ArrayList<>();
    List<Long> achievedGoalIds =
        request.getAchievedGoalIds() != null ? request.getAchievedGoalIds() : new ArrayList<>();

    for (ChallengeGoal challengeGoal : challengeGoals) {
      boolean isCompleted = achievedGoalIds.contains(challengeGoal.getId());
      DiaryGoal diaryGoal =
          DiaryGoal.builder()
              .diary(newDiary) // newDiary 이미 저장된 다이어리로 변경
              .challengeGoal(challengeGoal)
              .isCompleted(isCompleted)
              .build();
      diaryGoals.add(diaryGoal);
      newDiary.addDiaryGoal(diaryGoal);
    }
    diaryGoalRepository.saveAll(diaryGoals);
    return DiaryResponse.from(member, newDiary, challengeSummary);
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
    Challenge challenge =
        challengeRepository
            .findById(request.getChallengeId())
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));
    ChallengeSummaryResponse challengeSummary =
        challengeService.toChallengeSummary(challenge, memberId);

    Participant participant =
        participantRepository.findByMemberIdAndChallengeId(memberId, challenge.getId());
    if (participant == null) {
      throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND);
    }

    List<ChallengeGoal> challengeGoals = Objects.requireNonNull(participant).getChallengeGoals();
    List<DiaryGoal> diaryGoals = new ArrayList<>();
    List<Long> achievedGoalIds =
        request.getAchievedGoalIds() != null ? request.getAchievedGoalIds() : new ArrayList<>();

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
    return DiaryResponse.from(member, diary, challengeSummary);
  }

  @Transactional
  public DiaryResponse getDiary(Long diaryId) {
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    Member member = diary.getMember();
    ChallengeSummaryResponse response =
        challengeService.toChallengeSummary(diary.getChallenge(), member.getId());

    return DiaryResponse.from(member, diary, response);
  }

  @Transactional
  public List<DiaryResponse> getAllPublicDiaries() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    List<Diary> diaries = diaryRepository.findDiariesByIsPublic(Boolean.TRUE);
    List<DiaryResponse> diaryResponses = new ArrayList<>();

    for (Diary diary : diaries) {
      diaryResponses.add(
          DiaryResponse.from(
              member, diary, challengeService.toChallengeSummary(diary.getChallenge(), memberId)));
    }
    return diaryResponses;
  }

  @Transactional
  public Pagination<DiaryResponse> getPublicDiariesPage(Integer size, String cursor) {
    int limit = (size == null || size <= 0) ? 10 : Math.min(size, 100);

    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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
              member, diary, challengeService.toChallengeSummary(diary.getChallenge(), memberId)));
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
      Long memberId = CurrentUserContext.getCurrentMemberId();
      Member member =
          memberRepository
              .findById(memberId)
              .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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
                      member,
                      diary,
                      challengeService.toChallengeSummary(diary.getChallenge(), memberId)))
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
  public List<DiaryResponse> getMyDiaries() {
    try {
      Long memberId = CurrentUserContext.getCurrentMemberId();
      Member member =
          memberRepository
              .findById(memberId)
              .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      List<Diary> diaries = diaryRepository.findDiariesByMember_Id(memberId);
      List<DiaryResponse> diaryResponses = new ArrayList<>();
      for (Diary diary : diaries) {
        diaryResponses.add(
            DiaryResponse.from(
                member,
                diary,
                challengeService.toChallengeSummary(diary.getChallenge(), memberId)));
      }
      return diaryResponses;
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  @Transactional
  public String uploadDiaryFile(Long diaryId, MultipartFile file) throws IOException {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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
}
