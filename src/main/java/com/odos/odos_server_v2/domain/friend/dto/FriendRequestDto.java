package com.odos.odos_server_v2.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 신청 요청")
@Builder
@Getter
public class FriendRequestDto {

    @Schema(description = "상대방 회원 ID", example = "2")
    private Long toMemberId;
}
