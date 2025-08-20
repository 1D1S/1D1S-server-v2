package com.odos.odos_server_v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // security(AUTH)
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증되지 않은 접근입니다."),
  INVALID_AUTH_PRINCIPAL(
      HttpStatus.BAD_REQUEST, "AUTH-002", "SecurityContext에 잘못된 principal이 포함되어있습니다."),
  INVALID_SIGNUP_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-003", "지원하지 않는 소셜 로그인 provider입니다."),
  OAUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-004", "OAuth 제공자에서 사용자를 찾을 수 없습니다."),
  EMAIL_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-005", "해당 이메일의 사용자를 찾을 수 없습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-006", "RefreshToken이 잘못되었거나 형식이 올바르지 않습니다."),
  EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-007", "RefreshToken이 만료되었습니다."),
  REFRESH_TOKEN_NOT_FOUND(
      HttpStatus.UNAUTHORIZED, "AUTH-008", "해당 RefreshToken에 해당하는 사용자를 찾을 수 없습니다."),
  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-009", "Access Token이 만료되었습니다."),
  INVALID_JWT(HttpStatus.UNAUTHORIZED, "AUTH-010", "유효하지 않은 JWT입니다."),
  INVALID_OAUTH_EMAIL(HttpStatus.BAD_REQUEST, "AUTH-011", "소셜 로그인에서 이메일을 가져올 수 없습니다."),

  // member
  CATEGORY_EMPTY(HttpStatus.BAD_REQUEST, "USER-001", "관심 카테고리는 최소 1개 이상 선택해야 합니다."),
  CATEGORY_TOO_MANY(HttpStatus.BAD_REQUEST, "USER-002", "관심 카테고리는 최대 3개까지 선택할 수 있습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-003", "회원을 찾을 수 없습니다."),
  INVALID_NICKNAME_FORMAT(
      HttpStatus.BAD_REQUEST, "USER-004", "닉네임은 한글 또는 영어로 8자 이내이며, 특수문자는 사용할 수 없습니다."),

  // diary
  DIARY_NOT_CREATED(HttpStatus.NOT_FOUND, "DIARY-001", "다이어리를 생성하는것에 실패하였습니다."),
  DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY-002", "존재하지 않는 다이어리입니다."),
  DIARYLIKE_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "DIARY-003", "이미 좋아요를 눌렀습니다. 좋아요는 한번만 누르기 가능합니다"),
  DIARYLIKE_NOT_EXISTS(HttpStatus.NOT_FOUND, "DIARY-004", "좋아요 누른 전적이 없습니다. 새로 좋아요를 눌러주세요");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
