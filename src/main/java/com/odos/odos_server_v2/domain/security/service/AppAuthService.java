package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.dto.AppLoginResponse;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.oauth2.client.ProviderUserInfoClient;
import com.odos.odos_server_v2.domain.security.oauth2.info.OAuth2UserInfo;
import com.odos.odos_server_v2.domain.security.oauth2.info.OAuth2UserInfoFactory;
import com.odos.odos_server_v2.domain.security.oauth2.service.SocialMemberService;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 네이티브 앱 소셜 로그인. 앱이 provider access_token 을 보내면 userinfo 를 조회해 회원을 해석하고, 자체 access/refresh 토큰을
 * 발급한다. 쿠키를 세팅하지 않고 토큰을 응답 바디로 반환한다.
 */
@Service
@RequiredArgsConstructor
public class AppAuthService {
  private final ProviderUserInfoClient providerUserInfoClient;
  private final SocialMemberService socialMemberService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public AppLoginResponse login(SignupRoute signupRoute, String providerAccessToken) {
    Map<String, Object> attributes =
        providerUserInfoClient.fetchAttributes(signupRoute, providerAccessToken);

    OAuth2UserInfo userInfo = OAuth2UserInfoFactory.get(signupRoute, attributes);
    String email = userInfo.getEmail();
    if (email == null || email.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_OAUTH_EMAIL);
    }

    Member member = socialMemberService.findOrCreate(email, signupRoute, userInfo.getId());

    String accessToken = jwtTokenProvider.createAccessToken(member);
    String refreshToken = jwtTokenProvider.createRefreshToken(member);
    LocalDateTime expiresAt =
        jwtTokenProvider
            .extractExpiration(refreshToken)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));
    refreshTokenService.saveActiveToken(member, refreshToken, expiresAt);

    return AppLoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isProfileComplete(member.isProfileComplete())
        .build();
  }
}
