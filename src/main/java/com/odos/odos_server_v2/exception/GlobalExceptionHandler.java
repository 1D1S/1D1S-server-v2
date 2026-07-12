package com.odos.odos_server_v2.exception;

import com.odos.odos_server_v2.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  // 깨진 JSON·바디 내 enum 오타 등 읽을 수 없는 요청 바디는 클라이언트 오류이므로 400.
  // (핸들러가 없으면 아래 Exception 핸들러가 가로채 500 으로 응답한다.)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
    log.warn("요청 바디를 읽을 수 없음(형식 오류)", ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("400", "요청 바디 형식이 올바르지 않습니다."));
  }

  // @Valid 검증 실패는 400 + 실패한 필드 메시지로 응답한다(핸들러 없으면 500이 됨).
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));
    log.warn("요청 값 검증 실패: {}", detail);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("400", detail.isBlank() ? "요청 값이 올바르지 않습니다." : detail));
  }

  // 예상치 못한 500은 스택트레이스만으론 어떤 요청에서 터졌는지 알 수 없어 진단이 어렵다.
  // 요청 메서드/URI 와 예외 클래스명을 함께 남겨 로그 grep 만으로 엔드포인트·예외 유형을 특정한다.
  // (응답 바디에는 내부 예외 정보를 노출하지 않는다.)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(
      Exception ex, HttpServletRequest request) {
    String query = request.getQueryString();
    log.error(
        "Unexpected Exception 발생 [{} {}{}] {}: {}",
        request.getMethod(),
        request.getRequestURI(),
        query == null ? "" : "?" + query,
        ex.getClass().getName(),
        ex.getMessage(),
        ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("500", "Internal Server Error"));
  }
}
