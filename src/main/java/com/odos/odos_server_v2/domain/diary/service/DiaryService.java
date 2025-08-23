package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.diary.entity.DiaryReport;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryLikeRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryReportRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

  public DiaryResponse createDiary(Long memberId, DiaryRequest request) {
    Member member = memberRepository.findById(memberId).orElseThrow();
    // Challenge challenge = challengeRepository.findById(reqeust.getChallengeId);

    Diary diary =
        Diary.builder()
            .member(member)
            // .challenge()
            .completedDate(request.getAchievedDate())
            .title(request.getTitle())
            .content(request.getContent())
            .feeling(request.getFeeling())
            .isPublic(request.getIsPublic())
            .likes(new ArrayList<>())
            .build();

    Diary newDiary = diaryRepository.save(diary);
    return DiaryResponse.from(member, newDiary);
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
    //        Challenge challenge =
    // challengeRepository.findById(request.getChallengeId()).orElseThrow();
    //        List<Long> goalIds = request.getAchievedGoalIds();
    //        List<DiaryGoal> goals = new ArrayList<>();
    //        for (Long goalId : goalIds) {
    //            DiaryGoal goal = diaryGoalRepository.findById(goalId).orElseThrow();
    //            goals.add(goal);
    //        }
    diary.updateDiary(request);
    diaryRepository.save(diary);
    return DiaryResponse.from(member, diary);
  }

  public DiaryResponse getDiary(Long diaryId) {
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    Member member = diary.getMember();
    return DiaryResponse.from(member, diary);
  }

  @Transactional
  public List<DiaryResponse> getAllPublicDiaries() {
    try {
      Long memberId = CurrentUserContext.getCurrentMemberId();
      Member member = memberRepository.findById(memberId).orElseThrow();
      List<Diary> diaries = diaryRepository.findDiariesByIsPublic(Boolean.TRUE);
      List<DiaryResponse> diaryResponses = new ArrayList<>();
      for (Diary diary : diaries) {
        diaryResponses.add(DiaryResponse.from(member, diary));
      }
      return diaryResponses;
    } catch (Exception e) {
      log.info(e.getMessage());
      return null;
    }
  }

  public Boolean deleteDiary(Long diaryId) {
    diaryRepository
        .findById(diaryId)
        .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
    try {
      diaryRepository.deleteById(diaryId);
      return true;
    } catch (CustomException e) {
      return false;
    }
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
    return diaries.stream().limit(size).map(diary -> DiaryResponse.from(member, diary)).toList();
  }

  public Boolean reportDiary(ReportRequest request, Long memberId) {
    try {
      Member member = memberRepository.findById(memberId).orElseThrow();
      Diary diary = diaryRepository.findById(request.getDiaryId()).orElseThrow();
      DiaryReport diaryReport =
          DiaryReport.builder()
              .member(member)
              .diary(diary)
              .type(request.getReportType())
              .content(request.getContent())
              .build();
      diaryReportRepository.save(diaryReport);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
