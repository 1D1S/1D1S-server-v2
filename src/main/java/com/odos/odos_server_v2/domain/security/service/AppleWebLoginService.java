package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.dto.AppleWebLoginRequest;
import com.odos.odos_server_v2.domain.security.dto.NativeCredentialType;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeRequest;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.oauth2.OAuth2LoginResponse;
import com.odos.odos_server_v2.domain.security.oauth2.service.SocialMemberService;
import com.odos.odos_server_v2.domain.security.oauth2.verification.AppleOidcCredentialVerifier;
import com.odos.odos_server_v2.domain.security.oauth2.verification.VerifiedSocialUser;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer.IssuedSession;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 웹 Apple 로그인. 구글 웹 흐름({@link
 * com.odos.odos_server_v2.domain.security.oauth2.handler.OAuth2LoginSuccessHandler})과 동일하게 WEBVIEW
 * 세션을 HttpOnly 쿠키로 심고 {@link OAuth2LoginResponse}를 반환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleWebLoginService {
  private final AppleOidcCredentialVerifier appleVerifier;
  private final SocialMemberService socialMemberService;
  private final SessionTokenIssuer sessionTokenIssuer;
  private final NativeSessionService nativeSessionService;
  private final JwtTokenProvider jwtTokenProvider;

  public OAuth2LoginResponse login(AppleWebLoginRequest request, HttpServletResponse response) {
    // 검증기는 네이티브 교환 요청 형태를 받으므로 web 토큰을 동일 형태로 감싼다. (deviceId/nonce는 web에선 미사용)
    VerifiedSocialUser user =
        appleVerifier.verify(
            new NativeLoginExchangeRequest(
                SignupRoute.APPLE,
                NativeCredentialType.ID_TOKEN,
                request.identityToken(),
                null,
                null,
                "web"));

    // ponytail: authorizationCode는 Apple refresh token 폐기(회원 탈퇴 시 revoke)용 훅으로만 받아 둔다.
    // 실제 revoke가 필요해지면 AppleClientSecretGenerator로 client_secret을 만들어 Apple token/revoke 엔드포인트를 호출.
    if (request.authorizationCode() != null && !request.authorizationCode().isBlank()) {
      log.debug("Apple authorizationCode received (revoke hook, not wired yet)");
    }

    Member member = socialMemberService.findOrCreate(user.email(), user.provider(), user.subject());

    OAuth2LoginResponse.OAuth2LoginResponseBuilder responseBuilder =
        OAuth2LoginResponse.builder().isProfileComplete(member.isProfileComplete());

    // 구글 웹과 동일하게, Flutter WebView 브릿지용 nativeCodeChallenge가 오면 일회용 코드를 대신 발급한다.
    if (request.nativeCodeChallenge() != null) {
      if (!request.nativeCodeChallenge().matches("^[A-Za-z0-9_-]{43}$")) {
        throw new CustomException(ErrorCode.NATIVE_PKCE_VERIFICATION_FAILED);
      }
      NativeSessionCodeResponse loginCode =
          nativeSessionService.issueCode(member.getId(), request.nativeCodeChallenge());
      responseBuilder
          .nativeLoginCode(loginCode.code())
          .nativeLoginCodeExpiresInSeconds(loginCode.expiresInSeconds());
    } else {
      IssuedSession issued = sessionTokenIssuer.issue(member, SessionType.WEBVIEW);
      jwtTokenProvider.sendAccessAndRefreshToken(
          response, issued.accessToken(), issued.refreshToken());
    }

    return responseBuilder.build();
  }
}
