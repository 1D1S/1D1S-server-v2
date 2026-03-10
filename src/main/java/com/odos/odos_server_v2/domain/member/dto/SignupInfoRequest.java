package com.odos.odos_server_v2.domain.member.dto;

import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Schema(
    description = "회원가입 추가 정보 요청",
    example =
        """
        {
          "nickname": "홍길동",
          "profileUrl": "https://example.com/profile.jpg",
          "job": "STUDENT",
          "birth": "2000-01-01",
          "gender": "MALE",
          "isPublic": true,
          "category": ["DEV", "BOOK"]
        }
        """)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SignupInfoRequest {

  @Schema(description = "닉네임 (한글 또는 영어, 8자 이내, 특수문자 불가)", example = "홍길동")
  @NotNull
  private String nickname;

  @Schema(description = "프로필 이미지 URL (S3 도입 전 임시 필드)", example = "https://example.com/profile.jpg")
  private String profileUrl;

  @Schema(description = "직업", example = "STUDENT")
  @NotNull
  private Job job;

  @Schema(description = "생년월일", example = "2000-01-01")
  @NotNull
  private LocalDate birth;

  @Schema(description = "성별", example = "MALE")
  @NotNull
  private Gender gender;

  @Schema(description = "프로필 공개 여부", example = "true")
  @NotNull
  private Boolean isPublic;

  @Schema(description = "관심 카테고리 (1개 이상 3개 이하)", example = "[\"DEV\", \"BOOK\"]")
  @NotNull
  private List<Category> category;
}
