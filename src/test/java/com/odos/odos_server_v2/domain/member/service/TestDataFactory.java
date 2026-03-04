package com.odos.odos_server_v2.domain.member.service;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipantStatus;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 이 클래스는 테스트 코드나 DB 초기화 스크립트 작성 시 참조할 수 있도록,
 * 문제가 발생했던 상황의 데이터를 어떻게 만들어야 하는지 보여주기 위한 템플릿(가짜 데이터 생성기)입니다.
 */
public class TestDataFactory {

    public static void createProblematicScenario() {
        // 1. 유저 2명 생성 (방장, 그리고 버그를 겪는 테스트 유저)
        Member hostMember = Member.builder()
                .id(1L)
                .email("host@test.com")
                .role(MemberRole.USER)
                .nickname("HostUser")
                .challenges(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();

        Member testMember = Member.builder()
                .id(2L)
                .email("test@test.com")
                .role(MemberRole.USER)
                .nickname("TestUser")
                .challenges(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();

        // 2. 방장이 챌린지 1개 생성 (목표 3개짜리)
        Challenge hostChallenge = Challenge.builder()
                .id(100L)
                .title("방장이 만든 챌린지")
                .category(Category.DEV)
                .type(ChallengeType.FIXED)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .hostMember(hostMember)
                .build();

        Participant hostParticipant = Participant.builder()
                .id(100L)
                .member(hostMember)
                .challenge(hostChallenge)
                .status(ParticipantStatus.HOST)
                .challengeGoals(new ArrayList<>())
                .build();
        hostMember.getParticipants().add(hostParticipant);

        // 방장의 목표 3개 생성
        for (int i = 1; i <= 3; i++) {
            ChallengeGoal goal = ChallengeGoal.builder()
                    .id((long) i)
                    .content("방장 목표 " + i)
                    .participant(hostParticipant)
                    .build();
            hostParticipant.getChallengeGoals().add(goal);
        }

        // 3. 테스트 유저가 위 챌린지에 참여 신청 (PENDING 상태, 목표 3개가 복사됨)
        Participant pendingParticipant = Participant.builder()
                .id(200L)
                .member(testMember)
                .challenge(hostChallenge)
                .status(ParticipantStatus.PENDING) // 핵심: 대기 상태
                .challengeGoals(new ArrayList<>())
                .build();
        testMember.getParticipants().add(pendingParticipant);

        // PENDING 상태이지만 목표는 이미 생성됨 (기존 버그의 원인)
        for (int i = 4; i <= 6; i++) {
            ChallengeGoal goal = ChallengeGoal.builder()
                    .id((long) i)
                    .content("대기중인 유저 목표 " + i)
                    .participant(pendingParticipant)
                    .build();
            pendingParticipant.getChallengeGoals().add(goal);
        }

        // 4. 테스트 유저 본인이 호스트인 챌린지 1개 생성 (목표 4개짜리)
        Challenge testUserChallenge = Challenge.builder()
                .id(200L)
                .title("테스트 유저가 만든 챌린지")
                .category(Category.EXERCISE)
                .type(ChallengeType.FLEXIBLE)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .hostMember(testMember)
                .build();

        Participant testHostParticipant = Participant.builder()
                .id(300L)
                .member(testMember)
                .challenge(testUserChallenge)
                .status(ParticipantStatus.HOST) // 핵심: 정상 참여 상태
                .challengeGoals(new ArrayList<>())
                .build();
        testMember.getChallenges().add(testUserChallenge);
        testMember.getParticipants().add(testHostParticipant);

        // 테스트 유저 방장의 목표 4개 생성
        for (int i = 7; i <= 10; i++) {
            ChallengeGoal goal = ChallengeGoal.builder()
                    .id((long) i)
                    .content("내 챌린지 목표 " + i)
                    .participant(testHostParticipant)
                    .build();
            testHostParticipant.getChallengeGoals().add(goal);
        }

        /*
         * [기존 버그 발생 상황 요약 (수정 전)]
         *
         * 1. getMemberChallenge(testMemberId):
         *    - testMember.getChallenges() 만 가져옴.
         *    - 본인이 방장인 "테스트 유저가 만든 챌린지" 1개만 나옴. (진행중인 챌린지 1개로 표시됨)
         *
         * 2. getTodayGoalCount(testMemberId):
         *    - DB의 전체 ChallengeGoal 중, participant의 memberId == testMemberId 인 것을 찾음.
         *    - status가 PENDING이든 HOST이든 상관없이 무조건 카운트.
         *    - 대기중인 목표 3개 + 내가 방장인 목표 4개 = 총 7개가 프론트에 표시됨.
         *
         * 3. toChallengeResponse(hostChallenge, testMember):
         *    - PENDING 상태로 참여중이므로, FIXED 챌린지 로직에 따라 hostMember의 목표 (id 1, 2, 3)을 내려줌.
         *    - 유저가 일지 달성 요청을 보낼 때 id 1, 2, 3을 보내지만,
         *      실제 testMember의 Participant 안에는 id 4, 5, 6이 들어있어 달성 처리가 안됨.
         *
         * [수정 후 결과]
         * 1. getMemberChallenge: HOST뿐만 아니라 PARTICIPANT인 것도 가져오게 수정. (위 예시에선 1개만 HOST이므로 1개만 진행중인 챌린지로 정상 표시)
         * 2. getTodayGoalCount: PENDING 상태의 목표 3개는 제외되고, HOST인 4개만 카운트되어 정상적으로 "오늘의 목표 4개"가 표시됨.
         * 3. toChallengeResponse: PENDING 상태라면 아직 방장의 목표를 내려주는게 맞고, PARTICIPANT가 되는 순간 내 Participant의 목표(4, 5, 6)를 내려주게 됨.
         */
    }
}
