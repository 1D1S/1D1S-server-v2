package com.odos.odos_server_v2.domain.push.dto;

import com.odos.odos_server_v2.domain.push.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenUpsertRequest(
    @NotBlank String token, @NotNull DevicePlatform platform, @NotBlank String deviceId) {}
