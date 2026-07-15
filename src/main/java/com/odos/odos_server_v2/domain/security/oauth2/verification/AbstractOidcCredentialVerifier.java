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
  private final String audience;
  private final Set<String> issuers;

  protected AbstractOidcCredentialVerifier(
      SignupRoute provider, JwtDecoder jwtDecoder, String audience, Set<String> issuers) {
    this.provider = provider;
    this.jwtDecoder = jwtDecoder;
    this.audience = audience;
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

    if (!jwt.getAudience().contains(audience)
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
