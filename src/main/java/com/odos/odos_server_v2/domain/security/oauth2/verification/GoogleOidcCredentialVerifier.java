package com.odos.odos_server_v2.domain.security.oauth2.verification;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class GoogleOidcCredentialVerifier extends AbstractOidcCredentialVerifier {
  public GoogleOidcCredentialVerifier(@Value("${native-auth.google-audience}") String clientId) {
    super(
        SignupRoute.GOOGLE,
        NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build(),
        clientId,
        Set.of("https://accounts.google.com", "accounts.google.com"));
  }
}
