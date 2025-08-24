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

  // challenge
  CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHALLENGE_001", "챌린지를 찾을 수 없습니다."),
  ALREADY_APPLIED(HttpStatus.CONFLICT, "CHALLENGE_002", "이미 해당 챌린지에 신청한 상태입니다."),
  PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHALLENGE_003", "참여자를 찾을 수 없습니다."),
  NO_AUTHORITY(HttpStatus.UNAUTHORIZED, "CHALLENGE_004", "권한이 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
