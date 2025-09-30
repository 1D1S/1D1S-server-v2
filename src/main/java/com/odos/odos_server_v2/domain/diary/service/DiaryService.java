package com.odos.odos_server_v2.domain.diary.service;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
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
import java.util.*;
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
  private final ChallengeService challengeService;
  private final ChallengeGoalRepository challengeGoalRepository;
  private final ParticipantRepository participantRepository;

  @Transactional
  public DiaryResponse createDiary(Long memberId, DiaryRequest request) {
    try {
      Member member = memberRepository.findById(memberId).orElseThrow();
      Challenge challenge = challengeRepository.findById(request.getChallengeId()).orElseThrow();
      ChallengeSummaryResponse challengeSummary =
          challengeService.toChallengeSummary(challenge, memberId);

      Participant participant =
          participantRepository.findByMemberIdAndChallengeId(memberId, challenge.getId());
      if (participant == null) {
        /*CustomException 던지기 */
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

      // 3. 챌린지 목표를 기반으로 다이어리 목표 생성 및 저장
      List<ChallengeGoal> challengeGoals = Objects.requireNonNull(participant).getChallengeGoals();
      List<DiaryGoal> diaryGoals = new ArrayList<>();
      List<Long> achievedGoalIds =
          request.getAchievedGoalIds() != null ? request.getAchievedGoalIds() : new ArrayList<>();

      for (ChallengeGoal challengeGoal : challengeGoals) {
        boolean isCompleted = achievedGoalIds.contains(challengeGoal.getId());
        DiaryGoal diaryGoal =
            DiaryGoal.builder()
                .diary(newDiary) // newDiary로 변경!
                .challengeGoal(challengeGoal)
                .isCompleted(isCompleted)
                .build();
        diaryGoals.add(diaryGoal);
        newDiary.addDiaryGoal(diaryGoal);
      }
      diaryGoalRepository.saveAll(diaryGoals);
      return DiaryResponse.from(member, newDiary, challengeSummary);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Transactional
  public DiaryResponse updateDiary(Long memberId, Long diaryId, DiaryRequest request) {
    try {
      Member member = memberRepository.findById(memberId).orElseThrow();
      Diary diary = diaryRepository.findById(diaryId).orElseThrow();
      Challenge challenge = challengeRepository.findById(request.getChallengeId()).orElseThrow();
      ChallengeSummaryResponse challengeSummary =
          challengeService.toChallengeSummary(challenge, memberId);

      Participant participant =
          participantRepository.findByMemberIdAndChallengeId(memberId, challenge.getId());
      if (participant == null) {
        /*CustomException 던지기 */
      }

      List<ChallengeGoal> challengeGoals = Objects.requireNonNull(participant).getChallengeGoals();
      List<DiaryGoal> diaryGoals = new ArrayList<>();
      List<Long> achievedGoalIds =
          request.getAchievedGoalIds() != null ? request.getAchievedGoalIds() : new ArrayList<>();

      for (ChallengeGoal challengeGoal : challengeGoals) {
        boolean isCompleted = achievedGoalIds.contains(challengeGoal.getId());
        DiaryGoal diaryGoal =
            DiaryGoal.builder()
                .diary(diary) // newDiary로 변경!
                .challengeGoal(challengeGoal)
                .isCompleted(isCompleted)
                .build();
        diaryGoals.add(diaryGoal);
      }

      diary.updateDiary(request, challenge, diaryGoals);
      diaryRepository.save(diary);
      return DiaryResponse.from(member, diary, challengeSummary);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

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
    try {
      Long memberId = CurrentUserContext.getCurrentMemberId();
      Member member = memberRepository.findById(memberId).orElseThrow();
      List<Diary> diaries = diaryRepository.findDiariesByIsPublic(Boolean.TRUE);
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
    return diaries.stream()
        .limit(size)
        .map(
            diary ->
                DiaryResponse.from(
                    member,
                    diary,
                    challengeService.toChallengeSummary(diary.getChallenge(), memberId)))
        .toList();
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

  // 마이페이지 다이어리 조회를 위한 서비스 메서드
  @Transactional
  public List<DiaryResponse> getMyDiaries() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member = memberRepository.findById(memberId).orElseThrow();
    List<Diary> diaries = diaryRepository.findDiariesByMember_Id(memberId);
    List<DiaryResponse> diaryResponses = new ArrayList<>();
    for (Diary diary : diaries) {
      diaryResponses.add(
          DiaryResponse.from(
              member, diary, challengeService.toChallengeSummary(diary.getChallenge(), memberId)));
    }
    return diaryResponses;
  }
}
