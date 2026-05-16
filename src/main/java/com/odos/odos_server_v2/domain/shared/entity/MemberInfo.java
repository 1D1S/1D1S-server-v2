package com.odos.odos_server_v2.domain.shared.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "회원 정보")
@Builder
@AllArgsConstructor
@Getter
public class MemberInfo {
  @Schema(description = "아이다", example = "1")
  private Long memberId;

  @Schema(description = "닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImg;

  public static MemberInfo from(Member member) {
    return MemberInfo.builder().memberId(member.getId()).nickname(member.getNickname()).build();
  }
}
