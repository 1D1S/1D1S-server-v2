package com.odos.odos_server_v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "Unauthorized access");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
