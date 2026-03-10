package com.odos.odos_server_v2.domain.member.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "성별 (MALE: 남성, FEMALE: 여성, ETC: 기타)")
public enum Gender {
  MALE,
  FEMALE,
  ETC
}
