package com.odos.odos_server_v2.domain.member.service;

import com.odos.odos_server_v2.domain.challenge.service.ChallengeService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberStatus;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberDeleteService {

  private final MemberRepository memberRepository;
  private final ChallengeService challengeService;

  /** 1. 회원 탈퇴 요청 (Soft Delete) */
  @Transactional
  public void requestWithdraw() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    // 탈퇴 상태 변경
    member.withdraw();

    // 주최 중인 챌린지 host 위임
    challengeService.withdrawMemberLeaveChallenge(member.getId());

    // 일지 처리
  }

  @Transactional
  public void requestWithdrawTest() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    // 탈퇴 상태 변경
    member.withdraw();

    // 주최 중인 챌린지 host 위임
    challengeService.withdrawMemberLeaveChallengeHost(member.getId());
  }

  @Transactional
  public void processDeletion() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(7);

    List<Member> targets = memberRepository.findDeletableMembers(threshold);

    for (Member member : targets) {
      member.softDelete();
    }
  }

  @Transactional
  public void restoreMember() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    if (member.getStatus() == MemberStatus.WITHDRAWN
        && member.getDeletedAt().isAfter(LocalDateTime.now().minusDays(7))) {
      member.restore();
      challengeService.rejoinMemberRestoreIndividualChallenge(memberId);
    }
  }
}
