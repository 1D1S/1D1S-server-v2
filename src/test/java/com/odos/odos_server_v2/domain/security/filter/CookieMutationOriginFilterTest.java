package com.odos.odos_server_v2.domain.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieMutationOriginFilterTest {
  private final CookieMutationOriginFilter filter = new CookieMutationOriginFilter();

  @Test
  void allowsTrustedWebOrigin() throws Exception {
    MockHttpServletRequest request = request("/auth/token", "https://dev.1day1streak.com");
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    assertEquals(request, chain.getRequest());
  }

  @Test
  void rejectsUntrustedOrigin() throws Exception {
    MockHttpServletRequest request = request("/auth/logout", "https://attacker.example");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertEquals(403, response.getStatus());
  }

  @Test
  void ignoresNativeBearerEndpoints() throws Exception {
    MockHttpServletRequest request = request("/auth/native/token/refresh", null);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertEquals(request, chain.getRequest());
  }

  private MockHttpServletRequest request(String path, String origin) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
    if (origin != null) {
      request.addHeader("Origin", origin);
    }
    return request;
  }
}
