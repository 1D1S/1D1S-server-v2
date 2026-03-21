package com.odos.odos_server_v2.response;

import com.odos.odos_server_v2.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "에러 응답")
@Getter
@AllArgsConstructor
public class ErrorResponse {

  @Schema(description = "에러 코드", example = "CHALLENGE_001")
  private String code;

  @Schema(description = "에러 메시지", example = "챌린지를 찾을 수 없습니다.")
  private String message;

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
  }
}
