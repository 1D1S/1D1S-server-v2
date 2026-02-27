package com.odos.odos_server_v2.exception;

import com.odos.odos_server_v2.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus()) // HttpStatus 적용
        .body(ErrorResponse.of(errorCode)); // JSON 바디 반환
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {

    log.error("Unexpected Exception 발생", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("500", "Internal Server Error"));
  }
}
