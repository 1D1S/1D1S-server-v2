package com.odos.odos_server_v2.domain.member.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.domain.member.dto.CalendarStreakDto;
import com.odos.odos_server_v2.domain.member.dto.MyPageDto;
import com.odos.odos_server_v2.domain.member.dto.SideBarDto;
import com.odos.odos_server_v2.domain.member.dto.StreakDto;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {
  private final MemberRepository memberRepository;
  private final ChallengeGoalRepository challengeGoalRepository;
  private final ImageService imageService;
  private final ChallengeService challengeService;
  private final DiaryService diaryService;

  @Transactional
  public void editNickname(Long memberId, String nickname) {
    String regex = "^[가-힣a-zA-Z]{1,8}$";
    if (!nickname.matches(regex)) {
      throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
    }
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    member.updateNickname(nickname);
  }

  @Transactional
  public void editProfileImage(Long memberId, String objectKey) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    member.updateProfileImage(objectKey);
  }

  public MyPageDto getMyPage(Long id) {
    Member member =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return MyPageDto.builder()
        .nickname(member.getNickname())
        .profileUrl(imageService.getFileUrl(member.getProfileUrl()))
        .email(member.getEmail())
        .provider(member.getSignupRoute().name())
        .streak(getStreakByMemberId(id))
        .challengeList(challengeService.getMemberChallenge(id, id))
        .diaryList(diaryService.getMyDiaries())
        .build();
  }

  public MyPageDto getOtherMyPage(Long id) {
    Member member =
            memberRepository
                    .findById(id)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (member.getIsPublic()) {
      return MyPageDto.builder()
              .nickname(member.getNickname())
              .profileUrl(imageService.getFileUrl(member.getProfileUrl()))
              .email(member.getEmail())
              .provider(member.getSignupRoute().name())
              .streak(getStreakByMemberId(id))
              .challengeList(challengeService.getMemberChallenge(id, id))
              .diaryList(diaryService.getOtherPublicDiaries(id))
              .build();
    }
    else {
      // 비공개
      throw new CustomException(ErrorCode.MEMBER_PROFILE_PRIVATE);
    }
  }

  public SideBarDto getSideBar(Long id) {
    Member member =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return SideBarDto.builder()
        .nickname(member.getNickname())
        .profileUrl(imageService.getFileUrl(member.getProfileUrl()))
        .streakCount(calculateStreaks(member.getDiaries())[0])
        .todayGoalCount(getTodayGoalCount(id))
        .challengeList(challengeService.getMemberChallenge(id, id))
        .build();
  }

  public StreakDto getStreakByMemberId(Long id) {
    Member member =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    List<Diary> diaryList = member.getDiaries();

    int[] streaks = calculateStreaks(diaryList);

    int todayGoalCount = getTodayGoalCount(id);
    int currentStreak = streaks[0];
    int totalDiaryCount = diaryList.size();
    int totalGoalCount = 0;
    int currentMonthDiaryCount = 0;
    int currentMonthGoalCount = 0;
    int maxStreak = streaks[1];
    List<CalendarStreakDto> calendar = generateCalendar(diaryList);

    for (Diary d : diaryList) {
      totalGoalCount += d.getDiaryGoals().size();
      if (YearMonth.from(d.getCompletedDate()).equals(YearMonth.now())) {
        currentMonthDiaryCount += 1;
        currentMonthGoalCount += d.getDiaryGoals().size();
      }
    }
    return new StreakDto(
        todayGoalCount,
        currentStreak,
        totalDiaryCount,
        totalGoalCount,
        currentMonthDiaryCount,
        currentMonthGoalCount,
        maxStreak,
        calendar);
  }

  private List<CalendarStreakDto> generateCalendar(List<Diary> diaryList) {
    YearMonth currentMonth = YearMonth.now();

    // 날짜별 개수를 저장할 Map
    Map<LocalDate, Long> diaryCountByDate =
        diaryList.stream()
            .filter(diary -> YearMonth.from(diary.getCompletedDate()).equals(currentMonth))
            .collect(Collectors.groupingBy(Diary::getCompletedDate, Collectors.counting()));

    // Map을 DailyStreakDto 리스트로 변환
    return diaryCountByDate.entrySet().stream()
        .map(entry -> new CalendarStreakDto(entry.getKey(), entry.getValue().intValue()))
        .sorted(Comparator.comparing(CalendarStreakDto::getDate)) // 날짜순 정렬
        .toList();
  }

  public int getTodayGoalCount(Long memberId) {
    LocalDate today = LocalDate.now();

    List<ChallengeGoal> allGoals = challengeGoalRepository.findAll();

    return (int)
        allGoals.stream()
            .filter(
                goal -> {
                  Participant mc = goal.getParticipant();
                  if (mc == null || mc.getMember() == null || mc.getChallenge() == null)
                    return false;

                  // 1. 현재 사용자 여부
                  if (!mc.getMember().getId().equals(memberId)) return false;

                  // 2. 챌린지 진행 중 여부
                  Challenge challenge = mc.getChallenge();
                  LocalDate start = challenge.getStartDate();
                  LocalDate end = challenge.getEndDate();

                  boolean started = !start.isAfter(today); // startDate ≤ today
                  boolean notEnded =
                      (end == null) || !today.isAfter(end); // today ≤ endDate or no endDate

                  return started && notEnded;
                })
            .count();
  }

  private int[] calculateStreaks(List<Diary> diaryList) {

    if (diaryList.isEmpty()) {
      return new int[] {0, 0};
    }

    // 날짜 Set 생성 (중복 제거)
    Set<LocalDate> dates =
        diaryList.stream().map(Diary::getCompletedDate).collect(Collectors.toSet());

    LocalDate today = LocalDate.now();

    // currentStreak 계산
    int currentStreak = calculateCurrentStreak(dates, today);

    // maxStreak 계산
    int maxStreak = calculateMaxStreak(dates);

    return new int[] {currentStreak, maxStreak};
  }

  private int calculateCurrentStreak(Set<LocalDate> dates, LocalDate today) {

    // 오늘 작성했으면 오늘부터,
    // 아니면 어제부터 시작
    LocalDate baseDate = dates.contains(today) ? today : today.minusDays(1);

    int streak = 0;

    while (dates.contains(baseDate.minusDays(streak))) {
      streak++;
    }

    return streak;
  }

  private int calculateMaxStreak(Set<LocalDate> dates) {

    if (dates.isEmpty()) return 0;

    List<LocalDate> sorted = dates.stream().sorted().toList();

    int max = 1;
    int temp = 1;

    for (int i = 1; i < sorted.size(); i++) {
      if (sorted.get(i - 1).plusDays(1).equals(sorted.get(i))) {
        temp++;
      } else {
        max = Math.max(max, temp);
        temp = 1;
      }
    }

    return Math.max(max, temp);
  }
}
