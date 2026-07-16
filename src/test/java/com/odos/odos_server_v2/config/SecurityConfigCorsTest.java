package com.odos.odos_server_v2.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigCorsTest {
  private final SecurityConfig securityConfig =
      new SecurityConfig(null, null, null, null, null, null, null, null);
  private final CorsConfigurationSource source = securityConfig.corsConfigurationSource();

  @Test
  void allowsNullOriginOnlyForNativeBootstrap() {
    CorsConfiguration bootstrap = configurationFor("/auth/native/bootstrap");
    CorsConfiguration regularApi = configurationFor("/auth/native/login/exchange");

    assertEquals("null", bootstrap.checkOrigin("null"));
    assertNull(regularApi.checkOrigin("null"));
  }

  @Test
  void keepsTrustedWebOriginsAllowedForEveryEndpoint() {
    String origin = "https://dev.1day1streak.com";

    assertEquals(origin, configurationFor("/auth/native/bootstrap").checkOrigin(origin));
    assertEquals(origin, configurationFor("/members/me").checkOrigin(origin));
  }

  private CorsConfiguration configurationFor(String path) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
    return source.getCorsConfiguration(request);
  }
}
