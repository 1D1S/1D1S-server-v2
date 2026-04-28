package com.odos.odos_server_v2.domain.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비공개 챌린지 참여 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeJoinPrivateRequest {

  @Schema(description = "비공개 챌린지 비밀번호", example = "1234")
  private String password;

  @Schema(description = "챌린지 목표 목록 (FLEXIBLE 챌린지에서 사용)", example = "[\"알고리즘 1문제 풀기\"]")
  private List<String> goals;
}
