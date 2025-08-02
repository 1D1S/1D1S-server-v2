package com.odos.odos_server_v2.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode errorCode;

  /* throw new CustomException(ErrorCode.XXX)으로 에러 던지기 */
  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
