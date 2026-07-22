package com.odos.odos_server_v2.domain.security.oauth2.verification;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.security.dto.NativeCredentialType;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeRequest;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.Set;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

abstract class AbstractOidcCredentialVerifier implements NativeSocialCredentialVerifier {
  private final SignupRoute provider;
  private final JwtDecoder jwtDecoder;
  private final Set<String> audiences;
  private final Set<String> issuers;

  protected AbstractOidcCredentialVerifier(
      SignupRoute provider, JwtDecoder jwtDecoder, String audience, Set<String> issuers) {
    this(provider, jwtDecoder, Set.of(audience), issuers);
  }

  // Apple의 web(Services ID)과 app(bundle id)은 aud가 서로 다를 수 있어 복수 audience를 허용한다.
  protected AbstractOidcCredentialVerifier(
      SignupRoute provider, JwtDecoder jwtDecoder, Set<String> audiences, Set<String> issuers) {
    this.provider = provider;
    this.jwtDecoder = jwtDecoder;
    this.audiences = audiences;
    this.issuers = issuers;
  }

  @Override
  public SignupRoute provider() {
    return provider;
  }

  @Override
  public VerifiedSocialUser verify(NativeLoginExchangeRequest request) {
    if (request.credentialType() != NativeCredentialType.ID_TOKEN) {
      throw new CustomException(ErrorCode.NATIVE_PROVIDER_CREDENTIAL_INVALID);
    }

    Jwt jwt;
    try {
      jwt = jwtDecoder.decode(request.credential());
    } catch (JwtException | IllegalArgumentException e) {
      throw new CustomException(ErrorCode.NATIVE_PROVIDER_CREDENTIAL_INVALID);
    }

    if (jwt.getAudience().stream().noneMatch(audiences::contains)
        || jwt.getIssuer() == null
        || !issuers.contains(jwt.getIssuer().toString())) {
      throw new CustomException(ErrorCode.NATIVE_PROVIDER_CLAIM_MISMATCH);
    }
    if (request.nonce() != null
        && !request.nonce().isBlank()
        && !request.nonce().equals(jwt.getClaimAsString("nonce"))) {
      throw new CustomException(ErrorCode.NATIVE_PROVIDER_CLAIM_MISMATCH);
    }

    String subject = jwt.getSubject();
    String email = jwt.getClaimAsString("email");
    if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_OAUTH_EMAIL);
    }
    return new VerifiedSocialUser(provider, subject, email);
  }
}
