package com.odos.odos_server_v2.domain.friend.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 신청 응답")
@Builder
@Getter
public class FriendRequestResponseDto {

    @Schema(description = "친구 신청 ID", example = "1")
    private Long requestId;

    @Schema(description = "신청한 회원 ID", example = "2")
    private Long fromMemberId;

    @Schema(description = "신청한 회원 닉네임", example = "홍길동")
    private String fromMemberNickname;

    @Schema(description = "신청한 회원 프로필 URL", example = "https://example.com/profile.jpg")
    private String fromMemberProfileUrl;

    @Schema(description = "상태", example = "PENDING")
    private String status;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;
}
