package com.odos.odos_server_v2.domain.member.controller;

import com.odos.odos_server_v2.domain.member.dto.SignupInfoRequest;
import com.odos.odos_server_v2.domain.member.service.SignupService;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Void> completeSignupInfo(
      @RequestBody SignupInfoRequest request, @AuthenticationPrincipal MemberPrincipal principal) {

    signupService.completeSignupInfo(principal.getId(), request);

    return ResponseEntity.ok(null);
  }
}
