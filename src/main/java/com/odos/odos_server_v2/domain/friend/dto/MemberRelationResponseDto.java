package com.odos.odos_server_v2.domain.friend.dto;

import lombok.Builder;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 관계 상태 응답")
@Builder
@Getter
public class MemberRelationResponseDto {

    @Schema(description = "대상 회원 ID", example = "2")
    private Long memberId;

    @Schema(description = "관계 상태: FRIEND(친구), REQUEST_SENT(보낸 신청), REQUEST_RECEIVED(받은 신청), NONE(관계 없음), BLOCKED(차단됨)")
    private String relationStatus;
}
