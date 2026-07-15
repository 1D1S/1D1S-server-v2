package com.odos.odos_server_v2.domain.security.service;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.entity.NativeSessionCode;
import com.odos.odos_server_v2.domain.security.entity.NativeSessionCodePurpose;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import com.odos.odos_server_v2.domain.security.repository.NativeSessionCodeRepository;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer.IssuedSession;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NativeSessionService {
  static final long CODE_TTL_SECONDS = 60;
  private static final int CODE_BYTES = 32;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final NativeSessionCodeRepository nativeSessionCodeRepository;
  private final MemberRepository memberRepository;
  private final SessionTokenIssuer sessionTokenIssuer;
  private final Clock clock = Clock.systemUTC();

  @Transactional
  public NativeSessionCodeResponse issueCode(Long memberId, String codeChallenge) {
    return issueCode(memberId, NativeSessionCodePurpose.LOGIN_EXCHANGE, codeChallenge);
  }

  @Transactional
  public NativeSessionCodeResponse issueWebBootstrapCode(Long memberId) {
    return issueCode(memberId, NativeSessionCodePurpose.WEB_BOOTSTRAP, null);
  }

  private NativeSessionCodeResponse issueCode(
      Long memberId, NativeSessionCodePurpose purpose, String codeChallenge) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    String code = randomCode();
    nativeSessionCodeRepository.save(
        NativeSessionCode.issue(
            member,
            RefreshTokenFingerprint.of(code),
            purpose,
            codeChallenge,
            now,
            now.plusSeconds(CODE_TTL_SECONDS)));
    return new NativeSessionCodeResponse(code, CODE_TTL_SECONDS);
  }

  @Transactional
  public ReissuedTokens bootstrap(String code) {
    Member member = consume(code, NativeSessionCodePurpose.WEB_BOOTSTRAP);
    IssuedSession issued = sessionTokenIssuer.issue(member, SessionType.WEBVIEW);
    return new ReissuedTokens(issued.accessToken(), issued.refreshToken());
  }

  @Transactional
  public Member consumeLoginCode(String code, String codeVerifier) {
    NativeSessionCode sessionCode = findConsumable(code, NativeSessionCodePurpose.LOGIN_EXCHANGE);
    if (codeVerifier == null
        || sessionCode.getCodeChallenge() == null
        || !MessageDigest.isEqual(
            sessionCode.getCodeChallenge().getBytes(StandardCharsets.US_ASCII),
            pkceChallenge(codeVerifier).getBytes(StandardCharsets.US_ASCII))) {
      throw new CustomException(ErrorCode.NATIVE_PKCE_VERIFICATION_FAILED);
    }
    sessionCode.use(now());
    return sessionCode.getMember();
  }

  private Member consume(String code, NativeSessionCodePurpose expectedPurpose) {
    NativeSessionCode sessionCode = findConsumable(code, expectedPurpose);
    sessionCode.use(now());
    return sessionCode.getMember();
  }

  private NativeSessionCode findConsumable(String code, NativeSessionCodePurpose expectedPurpose) {
    LocalDateTime now = now();
    NativeSessionCode sessionCode =
        nativeSessionCodeRepository
            .findByCodeHashForUpdate(RefreshTokenFingerprint.of(code))
            .orElseThrow(() -> new CustomException(ErrorCode.NATIVE_SESSION_CODE_INVALID));

    if (sessionCode.isUsed()) {
      throw new CustomException(ErrorCode.NATIVE_SESSION_CODE_INVALID);
    }
    if (sessionCode.isExpired(now)) {
      throw new CustomException(ErrorCode.NATIVE_SESSION_CODE_EXPIRED);
    }
    if (sessionCode.getPurpose() != expectedPurpose) {
      throw new CustomException(ErrorCode.NATIVE_SESSION_CODE_INVALID);
    }

    return sessionCode;
  }

  private String pkceChallenge(String codeVerifier) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is unavailable", e);
    }
  }

  private LocalDateTime now() {
    return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
  }

  private String randomCode() {
    byte[] bytes = new byte[CODE_BYTES];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  @Scheduled(cron = "0 10 3 * * *", zone = "UTC")
  @Transactional
  public void deleteExpiredCodes() {
    LocalDateTime retentionCutoff =
        LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).minusDays(1);
    nativeSessionCodeRepository.deleteByExpiresAtBefore(retentionCutoff);
  }
}
