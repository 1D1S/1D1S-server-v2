package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.LOGIN_SUCCESS;

import com.odos.odos_server_v2.domain.security.dto.AppleWebLoginRequest;
import com.odos.odos_server_v2.domain.security.oauth2.OAuth2LoginResponse;
import com.odos.odos_server_v2.domain.security.service.AppleWebLoginService;
import com.odos.odos_server_v2.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 웹(브라우저) Apple 로그인. 앱(네이티브)은 {@code POST /auth/native/login/exchange} (provider=APPLE)를 사용한다. 구글 웹
 * 로그인과 동일하게 WEBVIEW 세션 쿠키를 심고 {@link OAuth2LoginResponse}를 반환한다.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "인증", description = "Apple 웹 로그인")
public class AppleLoginController {
  private final AppleWebLoginService appleWebLoginService;

  @Operation(
      summary = "Apple 웹 로그인",
      description = "Apple JS SDK의 identityToken을 검증하고 웹 세션 쿠키를 발급한다.")
  @PostMapping("/auth/apple/login")
  public ApiResponse<OAuth2LoginResponse> login(
      @Valid @RequestBody AppleWebLoginRequest request, HttpServletResponse response) {
    return success(LOGIN_SUCCESS, appleWebLoginService.login(request, response));
  }
}
