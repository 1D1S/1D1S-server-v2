package com.odos.odos_server_v2.domain.diary.service;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.diary.dto.DiaryGoalDto;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DiaryServiceTest {

  @Autowired private DiaryService diaryService;

  @Autowired private MemberRepository memberRepository;
  @Autowired private ChallengeRepository challengeRepository;
  @Autowired private ParticipantRepository participantRepository;
  @Autowired private ChallengeGoalRepository challengeGoalRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private DiaryGoalRepository diaryGoalRepository;
  @MockitoBean @Autowired private ImageService imageService;
  @MockitoBean @Autowired private ChallengeService challengeService;

  @Test
  void createDiaryWithFixedChallenge() {
    // given
    Member host = memberRepository.save(createMember("host@test.com", "host"));
    Member guest = memberRepository.save(createMember("guest@test.com", "guest"));

    Challenge fixedChallenge = challengeRepository.save(createFixedChallenge(host));

    Participant hostParticipant =
        participantRepository.save(createParticipant(host, fixedChallenge));

    Participant guestParticipant =
        participantRepository.save(createParticipant(guest, fixedChallenge));

    ChallengeGoal hostGoal1 =
        challengeGoalRepository.save(createChallengeGoal("물 2L 마시기", hostParticipant));
    ChallengeGoal hostGoal2 =
        challengeGoalRepository.save(createChallengeGoal("운동 30분 하기", hostParticipant));

    // 게스트의 추가적인 개인챌린지 목표 추가 (로직상 잘 돌아가는지 체크)
    ChallengeGoal guestGoal =
        challengeGoalRepository.save(createChallengeGoal("게스트 개인 목표", guestParticipant));

    when(imageService.getFileUrl(any())).thenReturn("https://test.com/profile.png");

    when(challengeService.toChallengeSummary(eq(fixedChallenge), eq(guest.getId())))
        .thenReturn(null);

    DiaryRequest request = new DiaryRequest();
    request.setChallengeId(fixedChallenge.getId());
    request.setAchievedDate(LocalDate.of(2026, 3, 19));
    request.setTitle("오늘 일지");
    request.setContent("열심히 했다");
    request.setFeeling(Feeling.HAPPY);
    request.setIsPublic(true);

    // host goal 2개 중 1개만 체크했다고 가정
    request.setAchievedGoalIds(List.of(hostGoal1.getId()));

    // when
    DiaryResponse response = diaryService.createDiary(guest.getId(), request);

    // then 1) host가 만든 fixed goal 목록 조회
    List<Long> expectedHostGoalIds =
        challengeGoalRepository.getFixedGoals(host.getId(), fixedChallenge.getId()).stream()
            .map(ChallengeGoal::getId)
            .toList();
    System.out.println("host의 목표");
    System.out.println(expectedHostGoalIds);

    // then 2) 실제 저장된 DiaryGoal 조회
    List<DiaryGoal> savedDiaryGoals = diaryGoalRepository.findAll();

    // 목표 1개 수행한 다이어리
    List<Long> actualDiaryGoalChallengeGoalIds =
        savedDiaryGoals.stream().map(dg -> dg.getChallengeGoal().getId()).toList();

    assertEquals(expectedHostGoalIds.size(), actualDiaryGoalChallengeGoalIds.size());

    assertEquals(
        expectedHostGoalIds.stream().collect(toSet()),
        actualDiaryGoalChallengeGoalIds.stream().collect(toSet()));

    Set<Long> completedGoalIds =
        savedDiaryGoals.stream()
            .filter(dg -> Boolean.TRUE.equals(dg.getIsCompleted()))
            .map(dg -> dg.getChallengeGoal().getId())
            .collect(toSet());

    assertEquals(Set.of(hostGoal1.getId()), completedGoalIds);
    System.out.println("실제 만들어진 다이어리목표 확인");
    System.out.println(completedGoalIds);

    // then 4) 생성된 diary의 작성자는 guest여야 함
    assertTrue(
        savedDiaryGoals.stream()
            .allMatch(dg -> dg.getDiary().getMember().getId().equals(guest.getId())));

    // response도 추가 검증
    Set<Long> responseGoalIds =
        response.getDiaryInfo().getDiaryGoal().stream()
            .map(DiaryGoalDto::getChallengeGoalId)
            .collect(toSet());
    System.out.println("생성된 일지목표 list");
    System.out.println(responseGoalIds);
  }

  private Member createMember(String email, String nickname) {
    return Member.builder().email(email).nickname(nickname).profileUrl("profile.png").build();
  }

  private Challenge createFixedChallenge(Member host) {
    return Challenge.builder()
        .title("고정 목표 챌린지")
        .category(Category.EXERCISE) // 실제 값으로 수정
        .startDate(LocalDate.of(2026, 3, 1))
        .endDate(LocalDate.of(2026, 3, 31))
        .maxParticipantsCnt(10)
        .goalType(GoalType.FIXED)
        .participationType(ParticipationType.GROUP)
        .description("host 목표 기준으로 진행")
        .hostMember(host)
        .participants(new ArrayList<>())
        .likes(new ArrayList<>())
        .diaries(new ArrayList<>())
        .build();
  }

  private Participant createParticipant(Member member, Challenge challenge) {
    return Participant.builder()
        .member(member)
        .challenge(challenge)
        .status(ParticipantStatus.PARTICIPANT)
        .challengeGoals(new ArrayList<>())
        .build();
  }

  private ChallengeGoal createChallengeGoal(String content, Participant participant) {
    return ChallengeGoal.builder()
        .content(content)
        .participant(participant)
        .diaryGoals(new ArrayList<>())
        .build();
  }
}
