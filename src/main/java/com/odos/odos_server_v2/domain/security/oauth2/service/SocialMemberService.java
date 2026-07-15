package com.odos.odos_server_v2.domain.security.oauth2.service;

import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소셜 로그인 사용자를 회원으로 해석한다. 웹(리다이렉트 OAuth)과 앱(네이티브 토큰 교환)이 동일한 find-or-create 규칙을 공유하도록 로직을 한 곳에 모은다.
 */
@Service
@RequiredArgsConstructor
public class SocialMemberService {
  private final MemberRepository memberRepository;

  @Transactional
  public Member findOrCreate(String email, SignupRoute signupRoute, String socialId) {
    return memberRepository
        .findBySignupRouteAndSocialId(signupRoute, socialId)
        .or(() -> memberRepository.findByEmailAndSignupRoute(email, signupRoute))
        .orElseGet(() -> createMember(email, signupRoute, socialId));
  }

  private Member createMember(String email, SignupRoute signupRoute, String socialId) {
    Member newMember =
        Member.builder()
            .email(email)
            .signupRoute(signupRoute)
            .socialId(socialId)
            .role(MemberRole.GUEST)
            .build();

    return memberRepository.save(newMember);
  }
}
