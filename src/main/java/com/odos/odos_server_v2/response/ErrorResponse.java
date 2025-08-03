package com.odos.odos_server_v2.response;

import com.odos.odos_server_v2.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
  private String code;
  private String message;

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
  }
}
