package com.odos.odos_server_v2.domain.member.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberDeleteService {

  private final MemberRepository memberRepository;

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
  }

  /** 2. 실제 삭제 (Hard Delete) - 탈퇴 후 7일 지난 회원 */
  @Transactional
  public void hardDelete(Member member) {
    memberRepository.delete(member);
  }

  /** 3. 스케줄러에서 실행 */
  @Transactional
  public void processDeletion() {

    LocalDateTime threshold = LocalDateTime.now().minusDays(7);

    List<Member> targets = memberRepository.findDeletableMembers(threshold);

    for (Member member : targets) {
      hardDelete(member);
    }
  }
}
