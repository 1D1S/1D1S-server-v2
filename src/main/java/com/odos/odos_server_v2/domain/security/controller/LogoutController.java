package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.LOGOUT;

import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LogoutController {
  private final MemberRepository memberRepository;

  @PostMapping("/logout")
  public ApiResponse<Void> logout() {
    Long memberId = CurrentUserContext.getCurrentMemberId();

    memberRepository
        .findById(memberId)
        .ifPresent(
            member -> {
              member.updateRefreshToken(null);
              memberRepository.save(member);
            });

    return success(LOGOUT);
  }
}
