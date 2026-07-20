package com.odos.odos_server_v2.domain.security.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 웹(브라우저) Apple 로그인 요청. 웹은 Apple JS SDK로 받은 {@code identityToken}을 서버로 POST한다. 앱(네이티브)은 기존 {@code
 * POST /auth/native/login/exchange} (provider=APPLE) 경로를 사용한다.
 *
 * <p>{@code name}/{@code email}은 Apple이 최초 로그인 때만 내려주므로 optional이다. {@code authorizationCode}는 향후
 * Apple 토큰 폐기(회원 탈퇴 시 revoke)용 훅으로만 받아 둔다.
 */
public record AppleWebLoginRequest(
    @NotBlank String identityToken,
    String authorizationCode,
    String name,
    String email,
    String nativeCodeChallenge) {}
