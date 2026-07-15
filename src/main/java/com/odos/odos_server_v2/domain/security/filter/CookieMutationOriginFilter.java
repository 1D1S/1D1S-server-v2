package com.odos.odos_server_v2.domain.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CookieMutationOriginFilter extends OncePerRequestFilter {
  private static final Set<String> PROTECTED_PATHS = Set.of("/auth/token", "/auth/logout");

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !HttpMethod.POST.matches(request.getMethod())
        || !PROTECTED_PATHS.contains(request.getRequestURI());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!isAllowedOrigin(request.getHeader("Origin"))) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request origin");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isAllowedOrigin(String origin) {
    if (origin == null || origin.isBlank()) {
      return false;
    }
    try {
      URI uri = URI.create(origin);
      String host = uri.getHost();
      return "https".equalsIgnoreCase(uri.getScheme())
          && host != null
          && (host.equals("1day1streak.com") || host.endsWith(".1day1streak.com"));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
