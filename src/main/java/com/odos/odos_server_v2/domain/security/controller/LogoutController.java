package com.odos.odos_server_v2.domain.security.controller;

import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.util.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LogoutController {
  private final MemberRepository memberRepository;

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    Long memberId = CurrentUserContext.getCurrentMemberId();

    memberRepository
        .findById(memberId)
        .ifPresent(
            member -> {
              member.updateRefreshToken(null);
              memberRepository.save(member);
            });

    return ResponseEntity.ok().build();
  }
}
