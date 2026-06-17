package com.odos.odos_server_v2.domain.admin.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.odos.odos_server_v2.domain.member.service.MemberService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자", description = "관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final MemberService memberService;

    @Operation(summary = "관리자 권한 부여", description = "특정 회원에게 관리자 권한을 부여합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "관리자 권한 부여 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{ \"message\": \"관리자 권한이 부여되었습니다.\" }")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 접근",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "요청자가 관리자가 아님 (ErrorCode: MEMBER_NOT_ADMIN)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{ \"code\": \"MEMBER_NOT_ADMIN\", \"message\": \"요청자가 관리자 권한이 없습니다.\" }")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "회원을 찾을 수 없음 또는 삭제 처리된 회원 (ErrorCode: MEMBER_DELETED)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{ \"code\": \"MEMBER_DELETED\", \"message\": \"삭제 처리된 회원입니다.\" }")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 관리자 권한 보유 (ErrorCode: MEMBER_IS_ADMIN)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{ \"code\": \"MEMBER_IS_ADMIN\", \"message\": \"이미 관리자 권한을 보유한 회원입니다.\" }")
            )
        )
    })
    @PostMapping("/members/{memberId}")
    public ApiResponse<Void> grantUserAdmin(@PathVariable Long memberId) {
        memberService.grantAdminAuthority(memberId);
        return ApiResponse.success(Message.MEMBER_GRANT_ADMIN);
    }
}
