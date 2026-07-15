package com.odos.odos_server_v2.domain.security.oauth2.verification;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.security.dto.NativeLoginExchangeRequest;

public interface NativeSocialCredentialVerifier {
  SignupRoute provider();

  VerifiedSocialUser verify(NativeLoginExchangeRequest request);
}
