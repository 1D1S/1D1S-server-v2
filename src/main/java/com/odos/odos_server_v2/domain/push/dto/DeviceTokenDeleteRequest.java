package com.odos.odos_server_v2.domain.push.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenDeleteRequest(@NotBlank String deviceId) {}
