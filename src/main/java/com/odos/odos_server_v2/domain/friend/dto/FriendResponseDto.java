package com.odos.odos_server_v2.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 응답")
@Builder
@Getter
public class FriendResponseDto {

    @Schema(description = "친구 회원 ID", example = "2")
    private Long memberId;

    @Schema(description = "친구 회원 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "친구 회원 프로필 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;
}
