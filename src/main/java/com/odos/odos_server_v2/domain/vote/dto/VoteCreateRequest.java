package com.odos.odos_server_v2.domain.vote.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.odos.odos_server_v2.domain.vote.entity.VoteSelectionType;
import com.odos.odos_server_v2.domain.vote.entity.VoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@Schema(description = "투표 등록 요청")
public class VoteCreateRequest {
  @NotBlank
  @Size(max = 255)
  @Schema(example = "다음 챌린지 주제는?", requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @NotNull
  @Schema(
      example = "SINGLE",
      allowableValues = {"SINGLE", "MULTIPLE"})
  private VoteSelectionType selectionType;

  @NotEmpty
  @Size(min = 2)
  @Schema(example = "[\"운동\", \"독서\", \"미라클 모닝\"]")
  private List<@NotBlank @Size(max = 255) String> options;

  @NotNull
  @Schema(
      example = "PUBLIC",
      allowableValues = {"PUBLIC", "ADMIN_SURVEY"})
  private VoteType voteType;

  @NotNull
  @Schema(example = "2026-07-16")
  private LocalDate startDate;

  @NotNull
  @Schema(example = "2026-07-20")
  private LocalDate endDate;
}
