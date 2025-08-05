package com.odos.odos_server_v2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "Unauthorized access"),
  INVALID_AUTH_PRINCIPAL(HttpStatus.BAD_REQUEST, "AUTH-002", "Invalid authentication principal"),
  INVALID_SIGNUP_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-003", "Unexpected signup provider"),
  OAUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-004", "User not found from OAuth provider"),
  EMAIL_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-005", "User not found with given email"),

  CATEGORY_EMPTY(HttpStatus.BAD_REQUEST, "USER-001", "At least one interest category must be selected"),
  CATEGORY_TOO_MANY(HttpStatus.BAD_REQUEST, "USER-002", "Up to 3 interest categories can be selected"),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-003", "Member not found"),
  INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "USER-004", "Nickname must be Korean or English, max 8 chars, no special characters");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
