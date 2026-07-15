package com.odos.odos_server_v2.domain.security.oauth2.verification;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class KakaoOidcCredentialVerifier extends AbstractOidcCredentialVerifier {
  public KakaoOidcCredentialVerifier(@Value("${native-auth.kakao-audience}") String appKey) {
    super(
        SignupRoute.KAKAO,
        NimbusJwtDecoder.withJwkSetUri("https://kauth.kakao.com/.well-known/jwks.json").build(),
        appKey,
        Set.of("https://kauth.kakao.com"));
  }
}
