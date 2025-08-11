package com.odos.odos_server_v2.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private String message;
  private T data;

  public ApiResponse(String msg) {
    this.message = msg;
  }

  public static <T> ApiResponse<T> success(String msg) {
    return new ApiResponse<>(msg);
  }

  public static <T> ApiResponse<T> success(String msg, T data) {
    return new ApiResponse<>(msg, data);
  }
}
