package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.dto.NativeCredentialType;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeRequest;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeResponse;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeResponse.NativeTokens;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.oauth2.service.SocialMemberService;
import com.odos.odos_server_v2.domain.security.oauth2.verification.NativeSocialCredentialVerifier;
import com.odos.odos_server_v2.domain.security.oauth2.verification.VerifiedSocialUser;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer.IssuedSession;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class NativeLoginService {
  private final Map<
          com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute,
          NativeSocialCredentialVerifier>
      verifiers;
  private final SocialMemberService socialMemberService;
  private final SessionTokenIssuer sessionTokenIssuer;
  private final NativeSessionService nativeSessionService;
  private final JwtTokenProvider jwtTokenProvider;

  public NativeLoginService(
      List<NativeSocialCredentialVerifier> verifierList,
      SocialMemberService socialMemberService,
      SessionTokenIssuer sessionTokenIssuer,
      NativeSessionService nativeSessionService,
      JwtTokenProvider jwtTokenProvider) {
    this.verifiers =
        new EnumMap<>(com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute.class);
    verifierList.forEach(verifier -> verifiers.put(verifier.provider(), verifier));
    this.socialMemberService = socialMemberService;
    this.sessionTokenIssuer = sessionTokenIssuer;
    this.nativeSessionService = nativeSessionService;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public NativeLoginExchangeResponse exchange(NativeLoginExchangeRequest request) {
    Member member;
    if (request.provider() == SignupRoute.NAVER
        && request.credentialType() == NativeCredentialType.LOGIN_CODE) {
      member = nativeSessionService.consumeLoginCode(request.credential(), request.codeVerifier());
    } else {
      NativeSocialCredentialVerifier verifier = verifiers.get(request.provider());
      if (verifier == null) {
        throw new CustomException(ErrorCode.NATIVE_PROVIDER_CREDENTIAL_INVALID);
      }
      VerifiedSocialUser user = verifier.verify(request);
      member = socialMemberService.findOrCreate(user.email(), user.provider(), user.subject());
    }
    IssuedSession nativeSession = sessionTokenIssuer.issue(member, SessionType.NATIVE);
    NativeSessionCodeResponse webCode = nativeSessionService.issueWebBootstrapCode(member.getId());

    return new NativeLoginExchangeResponse(
        new NativeTokens(
            nativeSession.accessToken(),
            nativeSession.refreshToken(),
            jwtTokenProvider.getAccessTokenExpirationPeriod() / 1000,
            jwtTokenProvider.getRefreshTokenExpirationPeriod() / 1000),
        webCode.code(),
        webCode.expiresInSeconds(),
        member.isProfileComplete());
  }
}
