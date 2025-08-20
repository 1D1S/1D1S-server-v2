package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
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
            // .likes(null)
            .build();

    Diary newDiary = diaryRepository.save(diary);
    return DiaryResponse.from(member, newDiary);
  }

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

  public List<DiaryResponse> getAllPublicDiaries() {
    List<Diary> diaries = diaryRepository.findDiariesByIsPublic(Boolean.TRUE);
    List<DiaryResponse> diaryResponses = new ArrayList<>();
    for (Diary diary : diaries) {
      diaryResponses.add(DiaryResponse.from(diary.getMember(), diary));
    }
    return diaryResponses;
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
}
