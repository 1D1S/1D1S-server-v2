package com.odos.odos_server_v2.domain.security.jwt;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Component
@Getter
@Slf4j
public class JwtTokenProvider {

  private static final String LEGACY_ACCESS_TOKEN_COOKIE_NAME = "access_token";
  private static final String LEGACY_REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

  private final MemberRepository memberRepository;

  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.access-token-exp-time}")
  private Long accessTokenExpirationPeriod;

  @Value("${jwt.refresh-token-exp-time}")
  private Long refreshTokenExpirationPeriod;

  @Value("${jwt.cookie.access-token-name:accessToken}")
  private String accessTokenCookieName;

  @Value("${jwt.cookie.refresh-token-name:refreshToken}")
  private String refreshTokenCookieName;

  @Value("${jwt.cookie.domain:}")
  private String cookieDomain;

  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String createAccessToken(Member member) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenExpirationPeriod);

    return Jwts.builder()
        .setSubject("AccessToken")
        .claim("id", member.getId())
        .claim("email", member.getEmail())
        .claim("role", member.getRole().name())
        .claim("provider", member.getSignupRoute().name())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken() {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTokenExpirationPeriod);

    return Jwts.builder()
        .setSubject("RefreshToken")
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public void sendAccessAndRefreshToken(
      HttpServletResponse response, String accessToken, String refreshToken) {
    response.setStatus(HttpServletResponse.SC_OK);
    addAccessTokenCookie(response, accessToken);
    addRefreshTokenCookie(response, refreshToken);
  }

  public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
    addCookie(response, accessTokenCookieName, accessToken, accessTokenExpirationPeriod);
  }

  public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    addCookie(response, refreshTokenCookieName, refreshToken, refreshTokenExpirationPeriod);
  }

  private void clearCookieVariants(HttpServletResponse response, String name) {
    addCookie(response, name, "", 0L, false);
    if (cookieDomain != null && !cookieDomain.isBlank()) {
      addCookie(response, name, "", 0L, true);
    }
  }

  public void clearTokenCookies(HttpServletResponse response) {
    clearCookieVariants(response, accessTokenCookieName);
    clearCookieVariants(response, refreshTokenCookieName);
    clearCookieVariants(response, LEGACY_ACCESS_TOKEN_COOKIE_NAME);
    clearCookieVariants(response, LEGACY_REFRESH_TOKEN_COOKIE_NAME);
  }

  private void addCookie(
      HttpServletResponse response, String name, String value, Long maxAgeMillis) {
    addCookie(response, name, value, maxAgeMillis, true);
  }

  private void addCookie(
      HttpServletResponse response,
      String name,
      String value,
      Long maxAgeMillis,
      boolean includeConfiguredDomain) {
    long maxAgeSeconds = maxAgeMillis <= 0 ? 0 : maxAgeMillis / 1000;

    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(maxAgeSeconds);

    if (includeConfiguredDomain && cookieDomain != null && !cookieDomain.isBlank()) {
      cookieBuilder.domain(cookieDomain);
    }

    ResponseCookie cookie = cookieBuilder.build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  public boolean isValidToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public boolean isExpired(String token) {
    try {
      return parseToken(token).getExpiration().before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return true;
    }
  }

  public Claims parseClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Claims parseToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public Optional<String> extractMemberId(String accessToken) {
    try {
      Object id = parseToken(accessToken).get("id");
      return Optional.ofNullable(id).map(Object::toString);
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public Optional<String> extractAccessToken(HttpServletRequest request) {
    return extractTokenFromCookie(request, accessTokenCookieName)
        .or(() -> extractTokenFromCookie(request, LEGACY_ACCESS_TOKEN_COOKIE_NAME))
        .or(() -> extractTokenFromHeader(request.getHeader(HttpHeaders.AUTHORIZATION)));
  }

  public Optional<String> extractRefreshToken(HttpServletRequest request) {
    return extractTokenFromCookie(request, refreshTokenCookieName)
        .or(() -> extractTokenFromCookie(request, LEGACY_REFRESH_TOKEN_COOKIE_NAME))
        .or(() -> extractTokenFromHeader(request.getHeader("Authorization-Refresh")));
  }

  private Optional<String> extractTokenFromCookie(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(cookie -> cookieName.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .filter(value -> !value.isBlank());
  }

  public Optional<String> extractTokenFromHeader(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return Optional.of(bearerToken.substring(7));
    }
    return Optional.empty();
  }

  public void updateRefreshToken(Long memberId, String refreshToken) {
    memberRepository
        .findById(memberId)
        .ifPresentOrElse(
            member -> {
              member.updateRefreshToken(refreshToken);
              memberRepository.save(member);
            },
            () -> {
              throw new CustomException(ErrorCode.EMAIL_USER_NOT_FOUND);
            });
  }
}
