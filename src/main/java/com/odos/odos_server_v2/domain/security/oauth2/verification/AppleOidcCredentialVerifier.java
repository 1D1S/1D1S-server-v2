package com.odos.odos_server_v2.domain.security.oauth2.verification;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Apple Sign In ID token 검증기. 웹(Apple JS SDK)과 앱(sign_in_with_apple)이 동일한 검증 경로를 공유한다. Apple은 사용자
 * 이름을 ID token에 담지 않으므로 회원 식별은 {@code sub} 기준이며 이메일은 최초 동의 시 토큰에 포함된다.
 *
 * <p>{@code native-auth.apple-audience}에는 web Services ID와 app bundle id를 콤마로 나열할 수 있다. 값이 비어 있으면
 * 부팅은 정상이며 로그인 시도 시에만 검증 실패한다.
 */
@Component
public class AppleOidcCredentialVerifier extends AbstractOidcCredentialVerifier {
  public AppleOidcCredentialVerifier(@Value("${native-auth.apple-audience:}") String audiences) {
    super(
        SignupRoute.APPLE,
        NimbusJwtDecoder.withJwkSetUri("https://appleid.apple.com/auth/keys").build(),
        parseAudiences(audiences),
        Set.of("https://appleid.apple.com"));
  }

  private static Set<String> parseAudiences(String csv) {
    if (csv == null || csv.isBlank()) {
      return Set.of("");
    }
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
  }
}
