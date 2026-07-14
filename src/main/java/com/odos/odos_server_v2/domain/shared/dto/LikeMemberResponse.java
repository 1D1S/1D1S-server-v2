package com.odos.odos_server_v2.domain.shared.dto;

import com.odos.odos_server_v2.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "좋아요를 누른 회원 요약")
@Builder
@Getter
public class LikeMemberResponse {

  @Schema(description = "회원 ID", example = "1")
  private Long memberId;

  @Schema(description = "닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImage;

  public static LikeMemberResponse from(Member member) {
    return LikeMemberResponse.builder()
        .memberId(member.getId())
        .nickname(member.getNickname())
        .profileImage(member.getProfileUrl())
        .build();
  }
}
