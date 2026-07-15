package com.odos.odos_server_v2.domain.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.entity.NativeSessionCode;
import com.odos.odos_server_v2.domain.security.entity.NativeSessionCodePurpose;
import com.odos.odos_server_v2.domain.security.repository.NativeSessionCodeRepository;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer.IssuedSession;
import com.odos.odos_server_v2.domain.security.service.TokenReissueService.ReissuedTokens;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NativeSessionServiceTest {
  @Mock private NativeSessionCodeRepository nativeSessionCodeRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private SessionTokenIssuer sessionTokenIssuer;
  @InjectMocks private NativeSessionService nativeSessionService;

  @Test
  void issueCodeStoresOnlyFingerprint() {
    Member member = Member.builder().id(1L).build();
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    NativeSessionCodeResponse response =
        nativeSessionService.issueCode(1L, "0123456789012345678901234567890123456789012");

    ArgumentCaptor<NativeSessionCode> captor = ArgumentCaptor.forClass(NativeSessionCode.class);
    verify(nativeSessionCodeRepository).save(captor.capture());
    assertNotNull(response.code());
    assertFalse(response.code().isBlank());
    assertEquals(NativeSessionService.CODE_TTL_SECONDS, response.expiresInSeconds());
    assertEquals(RefreshTokenFingerprint.of(response.code()), captor.getValue().getCodeHash());
    assertEquals(
        "0123456789012345678901234567890123456789012", captor.getValue().getCodeChallenge());
  }

  @Test
  void bootstrapConsumesCodeAndCreatesCookieTokens() {
    Member member = Member.builder().id(1L).build();
    NativeSessionCode code =
        NativeSessionCode.issue(
            member,
            RefreshTokenFingerprint.of("one-time-code"),
            NativeSessionCodePurpose.WEB_BOOTSTRAP,
            null,
            LocalDateTime.now(ZoneOffset.UTC),
            LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1));
    when(nativeSessionCodeRepository.findByCodeHashForUpdate(any())).thenReturn(Optional.of(code));
    when(sessionTokenIssuer.issue(
            member, com.odos.odos_server_v2.domain.security.entity.SessionType.WEBVIEW))
        .thenReturn(new IssuedSession("session-id", "access", "refresh"));

    ReissuedTokens tokens = nativeSessionService.bootstrap("one-time-code");

    assertEquals("access", tokens.accessToken());
    assertEquals("refresh", tokens.refreshToken());
    assertNotNull(code.getUsedAt());
    verify(sessionTokenIssuer)
        .issue(member, com.odos.odos_server_v2.domain.security.entity.SessionType.WEBVIEW);
  }

  @Test
  void bootstrapRejectsAlreadyUsedCodeBeforeIssuingTokens() {
    Member member = Member.builder().id(1L).build();
    NativeSessionCode code =
        NativeSessionCode.issue(
            member,
            RefreshTokenFingerprint.of("used-code"),
            NativeSessionCodePurpose.WEB_BOOTSTRAP,
            null,
            LocalDateTime.now(ZoneOffset.UTC),
            LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1));
    code.use(LocalDateTime.now(ZoneOffset.UTC));
    when(nativeSessionCodeRepository.findByCodeHashForUpdate(any())).thenReturn(Optional.of(code));

    CustomException exception =
        assertThrows(CustomException.class, () -> nativeSessionService.bootstrap("used-code"));

    assertEquals(ErrorCode.NATIVE_SESSION_CODE_INVALID, exception.getErrorCode());
    verify(sessionTokenIssuer, never()).issue(any(), any());
  }

  @Test
  void consumeLoginCodeVerifiesPkceBeforeUsingCode() {
    Member member = Member.builder().id(1L).build();
    NativeSessionCode code =
        NativeSessionCode.issue(
            member,
            RefreshTokenFingerprint.of("login-code"),
            NativeSessionCodePurpose.LOGIN_EXCHANGE,
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            LocalDateTime.now(ZoneOffset.UTC),
            LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1));
    when(nativeSessionCodeRepository.findByCodeHashForUpdate(any())).thenReturn(Optional.of(code));

    Member consumed =
        nativeSessionService.consumeLoginCode(
            "login-code", "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");

    assertEquals(member, consumed);
    assertNotNull(code.getUsedAt());
  }
}
