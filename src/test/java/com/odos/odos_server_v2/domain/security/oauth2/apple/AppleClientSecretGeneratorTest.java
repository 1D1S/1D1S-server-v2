package com.odos.odos_server_v2.domain.security.oauth2.apple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AppleClientSecretGeneratorTest {

  private static String pkcs8Pem(ECPrivateKey key) {
    String b64 = Base64.getEncoder().encodeToString(key.getEncoded());
    return "-----BEGIN PRIVATE KEY-----\n" + b64 + "\n-----END PRIVATE KEY-----\n";
  }

  private static KeyPair p256() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
    gen.initialize(new ECGenParameterSpec("secp256r1"));
    return gen.generateKeyPair();
  }

  @Test
  void generatesVerifiableClientSecretWithExpectedClaims() throws Exception {
    KeyPair kp = p256();
    String pem = pkcs8Pem((ECPrivateKey) kp.getPrivate());
    AppleClientSecretGenerator gen =
        new AppleClientSecretGenerator("TEAM123456", "com.odos.app", "KEY7654321", pem);

    assertTrue(gen.isConfigured());
    String jwt = gen.generate();

    var claims =
        Jwts.parserBuilder().setSigningKey(kp.getPublic()).build().parseClaimsJws(jwt).getBody();

    assertEquals("TEAM123456", claims.getIssuer());
    assertEquals("com.odos.app", claims.getSubject());
    assertEquals("https://appleid.apple.com", claims.getAudience());
  }

  @Test
  void unconfiguredThrowsInsteadOfBreakingBoot() {
    AppleClientSecretGenerator gen = new AppleClientSecretGenerator("", "", "", "");
    assertFalse(gen.isConfigured());
    assertThrows(IllegalStateException.class, gen::generate);
  }
}
