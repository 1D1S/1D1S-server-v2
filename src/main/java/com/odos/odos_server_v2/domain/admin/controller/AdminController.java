package com.odos.odos_server_v2.domain.admin.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.odos.odos_server_v2.domain.admin.dto.AdminMemberFilterRequest;
import com.odos.odos_server_v2.domain.admin.dto.AdminMemberResponseDto;
import com.odos.odos_server_v2.domain.admin.service.AdminService;
import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.service.MemberService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
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
  private final AdminService adminService;

  @Operation(summary = "관리자 권한 부여", description = "특정 회원에게 관리자 권한을 부여합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "관리자 권한 부여 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{ \"message\": \"관리자 권한이 부여되었습니다.\" }"))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "요청자가 관리자가 아님 (ErrorCode: MEMBER_NOT_ADMIN)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{ \"code\": \"MEMBER_NOT_ADMIN\", \"message\": \"요청자가 관리자 권한이 없습니다.\" }"))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음 또는 삭제 처리된 회원 (ErrorCode: MEMBER_DELETED)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{ \"code\": \"MEMBER_DELETED\", \"message\": \"삭제 처리된 회원입니다.\" }"))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "이미 관리자 권한 보유 (ErrorCode: MEMBER_IS_ADMIN)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{ \"code\": \"MEMBER_IS_ADMIN\", \"message\": \"이미 관리자 권한을 보유한 회원입니다.\" }")))
  })
  @PatchMapping("/members/{memberId}")
  public ApiResponse<Void> grantUserAdmin(@PathVariable Long memberId) {
    memberService.grantAdminAuthority(memberId);
    return ApiResponse.success(Message.MEMBER_GRANT_ADMIN);
  }

  @Operation(
      summary = "회원 목록 조회",
      description = "등록된 활성 회원 목록을 조회합니다. 성별, 직업 상태, 가입 경로로 필터링이 가능합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "회원 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                                {
                                  "message": "회원 목록 조회 성공했습니다.",
                                  "data": {
                                    "items": [
                                      {
                                        "memberId": 1,
                                        "nickname": "홍길동",
                                        "email": "hong@example.com",
                                        "signupRoute": "KAKAO",
                                        "createdAt": "2025-01-15T10:30:00",
                                        "job": "STUDENT",
                                        "gender": "MALE",
                                        "isAdmin" : true,
                                        "interestCategories": ["DEV", "HEALTH"],
                                        "diaryCount": 42,
                                        "createdChallengeCount": 3,
                                        "participatedChallengeCount": 8
                                      }
                                    ],
                                    "pageInfo": {
                                      "page": 0,
                                      "size": 20,
                                      "totalElements": 1,
                                      "totalPages": 1,
                                      "hasNextPage": false
                                    }
                                  }
                                }
                                """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/members")
  public ApiResponse<OffsetPagination<AdminMemberResponseDto>> getMembers(
      @RequestParam(required = false)
          @io.swagger.v3.oas.annotations.media.Schema(
              description = "성별 필터 (MALE, FEMALE, ETC)",
              example = "MALE")
          Gender gender,
      @RequestParam(required = false)
          @io.swagger.v3.oas.annotations.media.Schema(
              description = "직업 상태 필터 (STUDENT, WORKER)",
              example = "STUDENT")
          Job job,
      @RequestParam(required = false)
          @io.swagger.v3.oas.annotations.media.Schema(
              description = "가입한 SNS 필터 (KAKAO, NAVER, GOOGLE, APPLE)",
              example = "KAKAO")
          SignupRoute signupRoute,
      @RequestParam(defaultValue = "0")
          @io.swagger.v3.oas.annotations.media.Schema(
              description = "페이지 번호 (0부터 시작)",
              example = "0")
          int page,
      @RequestParam(defaultValue = "20")
          @io.swagger.v3.oas.annotations.media.Schema(description = "페이지 크기", example = "20")
          int size) {
    Pageable pageable = PageRequest.of(page, size);
    AdminMemberFilterRequest filterRequest = new AdminMemberFilterRequest(gender, job, signupRoute);
    Page<AdminMemberResponseDto> members = adminService.getMembers(filterRequest, pageable);
    return ApiResponse.success("회원 목록 조회 성공했습니다.", OffsetPagination.from(members));
  }
}
