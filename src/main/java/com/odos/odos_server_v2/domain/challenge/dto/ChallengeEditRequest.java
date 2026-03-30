package com.odos.odos_server_v2.domain.challenge.dto;

import com.odos.odos_server_v2.domain.shared.Enum.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Optional;
import lombok.*;

@Schema(description = "챌린지 수정 요청")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeEditRequest {
  @Schema(description = "챌린지 제목", example = "30일 코딩 챌린지")
  private Optional<String> title;

  @Schema(description = "챌린지 썸네일 이미지", example = "529fabd9-ae8e-4746-b82c-77725fe1a3ae")
  private Optional<String> thumbnailImage;

  @Schema(description = "챌린지 카테고리", example = "DEV")
  private Optional<Category> category;

  @Schema(description = "챌린지 설명", example = "매일 1시간씩 코딩 공부를 진행합니다.")
  private Optional<String> description;

  @Schema(description = "중도 참여 허용 여부", example = "true")
  private Optional<Boolean> allowMidJoin;

  @Schema(description = "최대 참여 인원", example = "10")
  private Optional<Integer> maxParticipantCnt;

  @Schema(description = "챌린지 목표 목록", example = "[\"알고리즘 1문제 풀기\", \"책 10페이지 읽기\"]")
  private Optional<List<String>> goals;
}
