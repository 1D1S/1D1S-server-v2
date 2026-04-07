package com.odos.odos_server_v2.domain.comment.controller;

import com.odos.odos_server_v2.domain.comment.dto.CommentRequest;
import com.odos.odos_server_v2.domain.comment.dto.CommentResponse;
import com.odos.odos_server_v2.domain.comment.service.CommentService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글", description = "댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

  private final CommentService commentService;

  @Operation(summary = "댓글 생성", description = "일지에 댓글을 생성한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "댓글 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "댓글 생성 성공했습니다.",
                              "data": {
                                "commentId": 1,
                                "content": "좋은 일지네요!",
                                "authorId": 1,
                                "authorNickname": "닉네임",
                                "authorProfileUrl": "https://...",
                                "createdAt": "2024-01-01T00:00:00",
                                "replyCount": 0
                              }
                            }
                            """))),
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
                            """)))
  })
  @PostMapping("/diaries/{diaryId}")
  public ApiResponse<CommentResponse> createComment(
      @PathVariable Long diaryId, @RequestBody CommentRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.COMMENT_CREATE_SUCCESS, commentService.createComment(memberId, diaryId, request));
  }

  @Operation(summary = "대댓글 생성", description = "댓글에 대댓글을 생성한다. 대댓글에 대댓글은 불가능하다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "대댓글 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CommentResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "대댓글 생성 성공했습니다.",
                              "data": {
                                "commentId": 2,
                                "content": "저도 동의해요!",
                                "authorId": 2,
                                "authorNickname": "닉네임2",
                                "authorProfileUrl": "https://...",
                                "createdAt": "2024-01-01T00:01:00",
                                "replyCount": 0
                              }
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "대댓글에는 대댓글 불가",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "COMMENT-003", "message": "대댓글에는 대댓글을 작성할 수 없습니다." }
                            """)))
  })
  @PostMapping("/{commentId}/replies")
  public ApiResponse<CommentResponse> createReply(
      @PathVariable Long commentId, @RequestBody CommentRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return ApiResponse.success(
        Message.REPLY_CREATE_SUCCESS, commentService.createReply(memberId, commentId, request));
  }

  @Operation(
      summary = "댓글/대댓글 삭제",
      description = "댓글 또는 대댓글을 soft delete한다. 부모 댓글 삭제 시 자식 대댓글도 함께 삭제된다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "댓글 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "message": "댓글 삭제 성공했습니다." }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "삭제 권한 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            { "code": "COMMENT-002", "message": "댓글 작성자가 아니어서 접근 권한이 없습니다." }
                            """)))
  })
  @DeleteMapping("/{commentId}")
  public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    commentService.deleteComment(memberId, commentId);
    return ApiResponse.success(Message.COMMENT_DELETE_SUCCESS);
  }

  @Operation(summary = "일지 댓글 목록 조회", description = "특정 일지의 부모 댓글 목록을 offset 페이징으로 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "댓글 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OffsetPagination.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "댓글 조회 성공했습니다.",
                              "data": {
                                "items": [],
                                "pageInfo": { "page": 0, "size": 10, "totalElements": 0, "totalPages": 0, "hasNextPage": false }
                              }
                            }
                            """)))
  })
  @GetMapping("/diaries/{diaryId}")
  public ApiResponse<OffsetPagination<CommentResponse>> getComments(
      @PathVariable Long diaryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.success(
        Message.COMMENT_GET_SUCCESS, commentService.getComments(diaryId, page, size));
  }

  @Operation(summary = "대댓글 목록 조회", description = "특정 댓글의 대댓글 목록을 offset 페이징으로 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "대댓글 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OffsetPagination.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                            {
                              "message": "대댓글 조회 성공했습니다.",
                              "data": {
                                "items": [],
                                "pageInfo": { "page": 0, "size": 10, "totalElements": 0, "totalPages": 0, "hasNextPage": false }
                              }
                            }
                            """)))
  })
  @GetMapping("/{commentId}/replies")
  public ApiResponse<OffsetPagination<CommentResponse>> getReplies(
      @PathVariable Long commentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ApiResponse.success(
        Message.REPLY_GET_SUCCESS, commentService.getReplies(commentId, page, size));
  }
}
