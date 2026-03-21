package com.odos.odos_server_v2.domain.member.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.SIGN_UP_INFO;

import com.odos.odos_server_v2.domain.member.dto.SignupInfoRequest;
import com.odos.odos_server_v2.domain.member.service.SignupService;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원가입", description = "회원가입 API")
@RestController
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {
  private final SignupService signupService;

  @Operation(
      summary = "회원가입 추가 정보 입력",
      description =
          "OAuth2 소셜 로그인 이후 닉네임, 직업, 생년월일, 성별, 관심 카테고리 등 추가 정보를 입력하여 회원가입을 완료한다."
              + " 닉네임은 한글 또는 영어로 8자 이내이며 특수문자는 불가하다."
              + " 관심 카테고리는 1개 이상 3개 이하로 선택해야 한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "회원가입 추가 정보 입력 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "회원가입 및 추가정보 입력 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "요청 값 오류 (닉네임 형식 오류, 카테고리 미선택 또는 초과)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "카테고리 미선택",
                      value =
                          """
                          { "code": "USER-001", "message": "관심 카테고리는 최소 1개 이상 선택해야 합니다." }
                          """),
                  @ExampleObject(
                      name = "카테고리 초과",
                      value =
                          """
                          { "code": "USER-002", "message": "관심 카테고리는 최대 3개까지 선택할 수 있습니다." }
                          """),
                  @ExampleObject(
                      name = "닉네임 형식 오류",
                      value =
                          """
                          { "code": "USER-004", "message": "닉네임은 한글 또는 영어로 8자 이내이며, 특수문자는 사용할 수 없습니다." }
                          """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "AUTH-001", "message": "인증되지 않은 접근입니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "USER-003", "message": "회원을 찾을 수 없습니다." }
                            """)))
  })
  @PutMapping("/info")
  public ApiResponse<Void> completeSignupInfo(
      @RequestBody SignupInfoRequest request, @AuthenticationPrincipal MemberPrincipal principal) {
    signupService.completeSignupInfo(principal.getId(), request);
    return success(SIGN_UP_INFO);
  }
}
