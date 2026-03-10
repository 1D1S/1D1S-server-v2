package com.odos.odos_server_v2.domain.member.entity.Enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직업 (STUDENT: 학생, WORKER: 직장인)")
public enum Job {
  STUDENT,
  WORKER
}
