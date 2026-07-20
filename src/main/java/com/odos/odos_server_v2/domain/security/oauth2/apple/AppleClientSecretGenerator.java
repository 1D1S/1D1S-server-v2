package com.odos.odos_server_v2.domain.security.oauth2.apple;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Apple에 authorizationCode를 교환하거나 refresh token을 폐기(revoke)할 때 필요한 client_secret(ES256 JWT)을 생성한다.
 * Apple은 정적 client secret을 쓰지 않고 .p8 키로 서명한 짧은 수명의 JWT를 요구한다.
 *
 * <p>env(APPLE_TEAM_ID / APPLE_CLIENT_ID / APPLE_KEY_ID / APPLE_PRIVATE_KEY)가 비어 있으면 빈은 그대로 뜨고,
 * {@link #generate()} 호출 시에만 실패한다. 즉 값만 채우면 동작한다.
 */
@Component
public class AppleClientSecretGenerator {
  private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
  // Apple 상한은 6개월. 여유를 두고 180일로 발급한다.
  private static final long TTL_DAYS = 180;

  private final String teamId;
  private final String clientId;
  private final String keyId;
  private final String privateKeyPem;

  public AppleClientSecretGenerator(
      @Value("${apple.team-id:}") String teamId,
      @Value("${apple.client-id:}") String clientId,
      @Value("${apple.key-id:}") String keyId,
      @Value("${apple.private-key:}") String privateKeyPem) {
    this.teamId = teamId;
    this.clientId = clientId;
    this.keyId = keyId;
    this.privateKeyPem = privateKeyPem;
  }

  public boolean isConfigured() {
    return !isBlank(teamId) && !isBlank(clientId) && !isBlank(keyId) && !isBlank(privateKeyPem);
  }

  public String generate() {
    if (!isConfigured()) {
      throw new IllegalStateException(
          "Apple client_secret 생성에 필요한 env(APPLE_TEAM_ID/CLIENT_ID/KEY_ID/PRIVATE_KEY)가 비어 있습니다.");
    }
    Instant now = Instant.now();
    return Jwts.builder()
        .setHeaderParam("kid", keyId)
        .setHeaderParam("alg", "ES256")
        .setIssuer(teamId)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(TTL_DAYS, ChronoUnit.DAYS)))
        .setAudience(APPLE_AUDIENCE)
        .setSubject(clientId)
        .signWith(parsePrivateKey(privateKeyPem), SignatureAlgorithm.ES256)
        .compact();
  }

  static PrivateKey parsePrivateKey(String pem) {
    String base64 =
        pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    try {
      byte[] der = Base64.getDecoder().decode(base64);
      return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(der));
    } catch (Exception e) {
      throw new IllegalStateException("APPLE_PRIVATE_KEY(.p8) 파싱에 실패했습니다.", e);
    }
  }

  private static boolean isBlank(String v) {
    return v == null || v.isBlank();
  }
}
