package com.odos.odos_server_v2.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "휴대폰 번호 변경 요청")
@Getter
@NoArgsConstructor
public class PhoneNumberRequest {

  @Schema(description = "휴대폰 번호 (하이픈 유무 무관, 예: 010-1234-5678)", example = "010-1234-5678")
  @NotBlank
  @Pattern(
      regexp = "^01[016-9]-?\\d{3,4}-?\\d{4}$",
      message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
  private String phoneNumber;
}
