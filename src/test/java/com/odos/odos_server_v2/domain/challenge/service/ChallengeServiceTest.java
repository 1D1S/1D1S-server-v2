package com.odos.odos_server_v2.domain.challenge.service;

import static org.junit.jupiter.api.Assertions.*;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeGoalRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.challenge.repository.ParticipantRepository;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.diary.repository.DiaryGoalRepository;
import com.odos.odos_server_v2.domain.diary.repository.DiaryRepository;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChallengeServiceTest {

  @Autowired private ChallengeService challengeService;

  @Autowired private MemberRepository memberRepository;
  @Autowired private ChallengeRepository challengeRepository;
  @Autowired private ParticipantRepository participantRepository;
  @Autowired private ChallengeGoalRepository challengeGoalRepository;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private DiaryGoalRepository diaryGoalRepository;
  @MockitoBean @Autowired private ImageService imageService;

  // ──────────────────────────────────────────────
  // leaveChallengeHost
  // ──────────────────────────────────────────────

  @Test
  void leaveChallengeHost_챌린지없음_예외발생() {
    assertThrows(
        CustomException.class,
        () -> challengeService.leaveChallengeHost(1L, 999L),
        ErrorCode.CHALLENGE_NOT_FOUND.name());
  }

  @Test
  void leaveChallengeHost_호스트아님_예외발생() {
    Member host = memberRepository.save(createMember("host@test.com", "host"));
    Challenge challenge = challengeRepository.save(createChallenge(host));
    assertThrows(
        CustomException.class,
        () -> challengeService.leaveChallengeHost(host.getId() + 1, challenge.getId()),
        ErrorCode.NO_AUTHORITY.name());
  }

  @Test
  void leaveChallengeHost_위임할참가자없음_예외발생() {
    // given: 챌린지에 HOST만 존재
    Member host = memberRepository.save(createMember("host@test.com", "host"));
    Challenge challenge = challengeRepository.save(createChallenge(host));
    participantRepository.save(createParticipant(host, challenge, ParticipantStatus.HOST));

    // when & then
    assertThrows(
        IllegalStateException.class,
        () -> challengeService.leaveChallengeHost(host.getId(), challenge.getId()));
  }

  @Test
  void leaveChallengeHost_다이어리목표수많은참가자로위임() {
    // given
    Member host = memberRepository.save(createMember("host@test.com", "호스트"));
    Member memberA = memberRepository.save(createMember("a@test.com", "나가"));
    Member memberB = memberRepository.save(createMember("b@test.com", "다나가"));

    Challenge challenge = challengeRepository.save(createChallenge(host));

    Participant hostParticipant =
        participantRepository.save(createParticipant(host, challenge, ParticipantStatus.HOST));
    Participant participantA =
        participantRepository.save(
            createParticipant(memberA, challenge, ParticipantStatus.PARTICIPANT));
    Participant participantB =
        participantRepository.save(
            createParticipant(memberB, challenge, ParticipantStatus.PARTICIPANT));

    // participantA: ChallengeGoal 1개 → DiaryGoal 3개
    ChallengeGoal goalA = challengeGoalRepository.save(createChallengeGoal("목표A", participantA));
    Diary diaryA = diaryRepository.save(createDiary(memberA, challenge));
    diaryGoalRepository.save(createDiaryGoal(diaryA, goalA));
    diaryGoalRepository.save(createDiaryGoal(diaryA, goalA));
    diaryGoalRepository.save(createDiaryGoal(diaryA, goalA));

    // participantB: ChallengeGoal 1개 → DiaryGoal 1개
    ChallengeGoal goalB = challengeGoalRepository.save(createChallengeGoal("목표B", participantB));
    Diary diaryB = diaryRepository.save(createDiary(memberB, challenge));
    diaryGoalRepository.save(createDiaryGoal(diaryB, goalB));

    // when
    challengeService.leaveChallengeHost(host.getId(), challenge.getId());

    // then
    Participant updatedHostParticipant =
        participantRepository
            .findByMemberIdAndChallengeId(host.getId(), challenge.getId())
            .orElseThrow();
    Participant updatedA =
        participantRepository
            .findByMemberIdAndChallengeId(memberA.getId(), challenge.getId())
            .orElseThrow();

    assertEquals(ParticipantStatus.LEAVE, updatedHostParticipant.getStatus());
    assertEquals(ParticipantStatus.HOST, updatedA.getStatus());

    Challenge updatedChallenge = challengeRepository.findById(challenge.getId()).orElseThrow();
    assertEquals(memberA.getId(), updatedChallenge.getHostMember().getId());
  }

  @Test
  void leaveChallengeHost_동점시_닉네임가나다순으로위임() {
    // given
    Member host = memberRepository.save(createMember("host@test.com", "호스트"));
    Member memberA = memberRepository.save(createMember("a@test.com", "나가")); // 가나다순 앞
    Member memberB = memberRepository.save(createMember("b@test.com", "다나가")); // 가나다순 뒤

    Challenge challenge = challengeRepository.save(createChallenge(host));

    participantRepository.save(createParticipant(host, challenge, ParticipantStatus.HOST));
    Participant participantA =
        participantRepository.save(
            createParticipant(memberA, challenge, ParticipantStatus.PARTICIPANT));
    Participant participantB =
        participantRepository.save(
            createParticipant(memberB, challenge, ParticipantStatus.PARTICIPANT));

    // participantA, participantB 각각 DiaryGoal 2개 (동점)
    ChallengeGoal goalA = challengeGoalRepository.save(createChallengeGoal("목표A", participantA));
    Diary diaryA = diaryRepository.save(createDiary(memberA, challenge));
    diaryGoalRepository.save(createDiaryGoal(diaryA, goalA));
    diaryGoalRepository.save(createDiaryGoal(diaryA, goalA));

    ChallengeGoal goalB = challengeGoalRepository.save(createChallengeGoal("목표B", participantB));
    Diary diaryB = diaryRepository.save(createDiary(memberB, challenge));
    diaryGoalRepository.save(createDiaryGoal(diaryB, goalB));
    diaryGoalRepository.save(createDiaryGoal(diaryB, goalB));

    // when
    challengeService.leaveChallengeHost(host.getId(), challenge.getId());

    // then: "나가" < "다나가" 이므로 memberA가 다음 호스트
    Participant updatedA =
        participantRepository
            .findByMemberIdAndChallengeId(memberA.getId(), challenge.getId())
            .orElseThrow();
    Participant updatedB =
        participantRepository
            .findByMemberIdAndChallengeId(memberB.getId(), challenge.getId())
            .orElseThrow();

    assertEquals(ParticipantStatus.HOST, updatedA.getStatus());
    assertEquals(ParticipantStatus.PARTICIPANT, updatedB.getStatus());

    Challenge updatedChallenge = challengeRepository.findById(challenge.getId()).orElseThrow();
    assertEquals(memberA.getId(), updatedChallenge.getHostMember().getId());
  }

  @Test
  void leaveChallengeHost_다이어리목표없을때_첫번째참가자로위임() {
    // given: 참가자들이 DiaryGoal이 전혀 없는 경우 → count가 모두 0 → 닉네임순 첫 번째
    Member host = memberRepository.save(createMember("host@test.com", "호스트"));
    Member memberA = memberRepository.save(createMember("a@test.com", "가나다"));
    Member memberB = memberRepository.save(createMember("b@test.com", "나다라"));

    Challenge challenge = challengeRepository.save(createChallenge(host));

    participantRepository.save(createParticipant(host, challenge, ParticipantStatus.HOST));
    Participant participantA =
        participantRepository.save(
            createParticipant(memberA, challenge, ParticipantStatus.PARTICIPANT));
    participantRepository.save(
        createParticipant(memberB, challenge, ParticipantStatus.PARTICIPANT));

    // when
    challengeService.leaveChallengeHost(host.getId(), challenge.getId());

    // then: 모두 0점이므로 닉네임 가나다순 "가나다"가 위임
    Participant updatedA =
        participantRepository
            .findByMemberIdAndChallengeId(memberA.getId(), challenge.getId())
            .orElseThrow();
    assertEquals(ParticipantStatus.HOST, updatedA.getStatus());

    Challenge updatedChallenge = challengeRepository.findById(challenge.getId()).orElseThrow();
    assertEquals(memberA.getId(), updatedChallenge.getHostMember().getId());
  }

  // ──────────────────────────────────────────────
  // 헬퍼 메서드
  // ──────────────────────────────────────────────

  private Member createMember(String email, String nickname) {
    return Member.builder().email(email).nickname(nickname).profileUrl("profile.png").build();
  }

  private Challenge createChallenge(Member host) {
    return Challenge.builder()
        .title("테스트 챌린지")
        .category(Category.EXERCISE)
        .startDate(LocalDate.of(2026, 1, 1))
        .endDate(LocalDate.of(2026, 12, 31))
        .maxParticipantsCnt(10)
        .type(GoalType.FLEXIBLE)
        .description("설명")
        .hostMember(host)
        .participants(new ArrayList<>())
        .likes(new ArrayList<>())
        .diaries(new ArrayList<>())
        .build();
  }

  private Participant createParticipant(
      Member member, Challenge challenge, ParticipantStatus status) {
    return Participant.builder()
        .member(member)
        .challenge(challenge)
        .status(status)
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

  private Diary createDiary(Member member, Challenge challenge) {
    return Diary.builder()
        .title("일지")
        .content("내용")
        .completedDate(LocalDate.of(2026, 4, 1))
        .feeling(Feeling.HAPPY)
        .member(member)
        .challenge(challenge)
        .build();
  }

  private DiaryGoal createDiaryGoal(Diary diary, ChallengeGoal challengeGoal) {
    return DiaryGoal.builder().diary(diary).challengeGoal(challengeGoal).build();
  }
}
