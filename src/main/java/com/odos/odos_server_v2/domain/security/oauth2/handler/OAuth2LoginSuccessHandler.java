package com.odos.odos_server_v2.domain.security.oauth2.handler;

import static com.odos.odos_server_v2.response.Message.LOGIN_SUCCESS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.member.service.MemberDeleteService;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.oauth2.OAuth2LoginResponse;
import com.odos.odos_server_v2.domain.security.service.NativeSessionService;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer.IssuedSession;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final MemberRepository memberRepository;
  private final MemberDeleteService memberDeleteService;
  private final SessionTokenIssuer sessionTokenIssuer;
  private final NativeSessionService nativeSessionService;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
    String email = principal.getEmail();
    SignupRoute signupRoute = principal.getSignupRoute();
    Member member =
        memberRepository
            .findByEmailAndSignupRoute(email, signupRoute)
            .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_USER_NOT_FOUND));

    String nativeCodeChallenge = request.getParameter("nativeCodeChallenge");
    OAuth2LoginResponse.OAuth2LoginResponseBuilder responseBuilder =
        OAuth2LoginResponse.builder().isProfileComplete(member.isProfileComplete());
    if (nativeCodeChallenge != null) {
      if (!nativeCodeChallenge.matches("^[A-Za-z0-9_-]{43}$")) {
        throw new CustomException(ErrorCode.NATIVE_PKCE_VERIFICATION_FAILED);
      }
      NativeSessionCodeResponse loginCode =
          nativeSessionService.issueCode(member.getId(), nativeCodeChallenge);
      responseBuilder
          .nativeLoginCode(loginCode.code())
          .nativeLoginCodeExpiresInSeconds(loginCode.expiresInSeconds());
    } else {
      IssuedSession issued = sessionTokenIssuer.issue(member, SessionType.WEBVIEW);
      jwtTokenProvider.sendAccessAndRefreshToken(
          response, issued.accessToken(), issued.refreshToken());
    }

    OAuth2LoginResponse dto = responseBuilder.build();

    response.setContentType("application/json;charset=UTF-8");
    ApiResponse<OAuth2LoginResponse> apiResponse = ApiResponse.success(LOGIN_SUCCESS, dto);
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

    // 탈퇴 처리 이후 7일 이전 로그인 시
    memberDeleteService.restoreMember();
  }
}
