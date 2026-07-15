package com.odos.odos_server_v2.domain.security.jwt;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/oauth2/")
        || path.startsWith("/auth/token")
        || path.startsWith("/auth/app/")
        || path.equals("/auth/native/bootstrap")
        || path.equals("/auth/native/login/exchange")
        || path.equals("/auth/native/token/refresh")
        || path.equals("/login");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Optional<String> tokenOpt = jwtTokenProvider.extractAccessToken(request);

    if (tokenOpt.isPresent()) {
      String token = tokenOpt.get();

      Claims claims = jwtTokenProvider.parseAccessToken(token);

      Optional.ofNullable(claims.get("id"))
          .map(Object::toString)
          .map(Long::parseLong)
          .flatMap(memberRepository::findById)
          .ifPresent(member -> setAuthentication(member, claims));
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(Member member, Claims claims) {
    SessionType sessionType = parseSessionType(claims);
    MemberPrincipal principal =
        new MemberPrincipal(
            member.getId(),
            member.getEmail(),
            member.getRole().name(),
            member.getSignupRoute(),
            sessionType,
            claims.get("sid", String.class));

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private SessionType parseSessionType(Claims claims) {
    String value = claims.get("session_type", String.class);
    if (value == null) {
      return SessionType.WEBVIEW;
    }
    try {
      return SessionType.valueOf(value);
    } catch (IllegalArgumentException e) {
      return SessionType.WEBVIEW;
    }
  }
}
