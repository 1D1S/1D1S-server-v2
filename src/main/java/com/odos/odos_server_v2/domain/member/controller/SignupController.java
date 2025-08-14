package com.odos.odos_server_v2.domain.member.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.SIGN_UP_INFO;

import com.odos.odos_server_v2.domain.member.dto.SignupInfoRequest;
import com.odos.odos_server_v2.domain.member.service.SignupService;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {
  private final SignupService signupService;

  @PutMapping("/info")
  public ApiResponse<Void> completeSignupInfo(
      @RequestBody SignupInfoRequest request, @AuthenticationPrincipal MemberPrincipal principal) {

    signupService.completeSignupInfo(principal.getId(), request);

    return success(SIGN_UP_INFO);
  }
}
