package com.odos.odos_server_v2.domain.security.dto;

import jakarta.validation.constraints.NotBlank;

public record NativeSessionBootstrapRequest(@NotBlank String code) {}
