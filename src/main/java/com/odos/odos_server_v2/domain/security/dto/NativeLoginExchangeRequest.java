package com.odos.odos_server_v2.domain.security.dto;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NativeLoginExchangeRequest(
    @NotNull SignupRoute provider,
    @NotNull NativeCredentialType credentialType,
    @NotBlank String credential,
    String nonce,
    String codeVerifier,
    @NotBlank String deviceId) {}
