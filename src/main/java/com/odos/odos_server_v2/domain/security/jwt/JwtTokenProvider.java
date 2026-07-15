package com.odos.odos_server_v2.domain.security.jwt;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtTokenProvider {

  public static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
  public static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";

  private static final String LEGACY_ACCESS_TOKEN_COOKIE_NAME = "access_token";
  private static final String LEGACY_REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

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
    return createAccessToken(member, SessionType.WEBVIEW, null);
  }

  public String createAccessToken(Member member, SessionType sessionType, String sessionId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenExpirationPeriod);

    var builder =
        Jwts.builder()
            .setSubject(ACCESS_TOKEN_SUBJECT)
            .claim("id", member.getId())
            .claim("email", member.getEmail())
            .claim("role", member.getRole().name())
            .claim("provider", member.getSignupRoute().name())
            .claim("session_type", sessionType.name())
            .setIssuedAt(now)
            .setExpiration(expiry);
    if (sessionId != null) {
      builder.claim("sid", sessionId);
    }
    return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
  }

  public String createRefreshToken(Member member) {
    return createRefreshToken(member, SessionType.WEBVIEW, null);
  }

  public String createRefreshToken(Member member, SessionType sessionType, String sessionId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTokenExpirationPeriod);

    var builder =
        Jwts.builder()
            .setSubject(REFRESH_TOKEN_SUBJECT)
            .setId(UUID.randomUUID().toString())
            .claim("id", member.getId())
            .claim("session_type", sessionType.name())
            .setIssuedAt(now)
            .setExpiration(expiry);
    if (sessionId != null) {
      builder.claim("sid", sessionId);
    }
    return builder.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
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

  public Claims parseAccessToken(String token) {
    return parseTypedToken(
        token, ACCESS_TOKEN_SUBJECT, ErrorCode.EXPIRED_ACCESS_TOKEN, ErrorCode.INVALID_JWT);
  }

  public Claims parseRefreshToken(String token) {
    return parseTypedToken(
        token,
        REFRESH_TOKEN_SUBJECT,
        ErrorCode.EXPIRED_REFRESH_TOKEN,
        ErrorCode.INVALID_REFRESH_TOKEN);
  }

  private Claims parseTypedToken(
      String token, String expectedSubject, ErrorCode expiredCode, ErrorCode invalidCode) {
    try {
      Claims claims = parseToken(token);
      if (!expectedSubject.equals(claims.getSubject())) {
        throw new CustomException(invalidCode);
      }
      return claims;
    } catch (ExpiredJwtException e) {
      throw new CustomException(expiredCode);
    } catch (JwtException | IllegalArgumentException e) {
      throw new CustomException(invalidCode);
    }
  }

  private Claims parseToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public Optional<LocalDateTime> extractExpiration(String token) {
    try {
      return Optional.of(parseToken(token).getExpiration())
          .map(Date::toInstant)
          .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
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
}
