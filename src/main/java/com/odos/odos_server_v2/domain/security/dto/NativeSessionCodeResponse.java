package com.odos.odos_server_v2.domain.security.dto;

public record NativeSessionCodeResponse(String code, long expiresInSeconds) {}
