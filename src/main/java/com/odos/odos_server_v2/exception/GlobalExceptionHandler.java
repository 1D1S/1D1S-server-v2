package com.odos.odos_server_v2.exception;

import com.odos.odos_server_v2.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    log.warn("CustomException 발생", ex);
    return ResponseEntity.status(errorCode.getStatus()) // HttpStatus 적용
        .body(ErrorResponse.of(errorCode)); // JSON 바디 반환
  }

  // 잘못된 타입(enum 오입력 등)·필수 파라미터 누락은 클라이언트 오류이므로 500 이 아닌 400 으로 응답한다.
  // (RestControllerAdvice 의 Exception 핸들러가 스프링 기본 400 처리를 가로채 500 으로 만드는 것을 방지)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("요청 파라미터 타입 불일치", ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("400", "요청 파라미터 '" + ex.getName() + "' 형식이 올바르지 않습니다."));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException ex) {
    log.warn("필수 요청 파라미터 누락", ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("400", "필수 요청 파라미터 '" + ex.getParameterName() + "' 가 누락되었습니다."));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
    log.error("Unexpected Exception 발생", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("500", "Internal Server Error"));
  }
}
