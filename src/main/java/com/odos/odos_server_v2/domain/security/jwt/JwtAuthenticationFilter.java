package com.odos.odos_server_v2.domain.security.jwt;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    return path.startsWith("/oauth2/") || path.startsWith("/auth/token") || path.equals("/login");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    jwtTokenProvider
        .extractAccessToken(request)
        .filter(jwtTokenProvider::isValidToken)
        .filter(token -> !jwtTokenProvider.isExpired(token))
        .flatMap(jwtTokenProvider::extractMemberId)
        .map(Long::parseLong)
        .flatMap(memberRepository::findById)
        .ifPresent(this::setAuthentication);

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(Member member) {
    MemberPrincipal principal =
        new MemberPrincipal(
            member.getId(), member.getEmail(), member.getRole().name(), member.getSignupRoute());

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
