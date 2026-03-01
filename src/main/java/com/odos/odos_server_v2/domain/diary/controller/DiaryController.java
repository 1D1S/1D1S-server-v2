package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.ReportRequest;
import com.odos.odos_server_v2.domain.diary.service.DiaryService;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.shared.dto.Pagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries")
@Slf4j
@Tag(name = "Diary API", description = "다이어리 관련 API")
public class DiaryController {
  private final DiaryService diaryService;

  @Operation(summary = "일지 생성", description = "현재 로그인한 사용자의 토큰을 기반으로 일지를 생성한다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "일지 생성 요청 DTO",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = DiaryRequest.class),
              examples =
                  @ExampleObject(
                      name = "일지 생성 예시",
                      value =
                          """
                                {
                                    "challengeId" : 1,
                                    "title" : "나의 다이어리",
                                    "content" : "완수하기",
                                    "feeling" : "HAPPY",
                                    "isPublic" : true,
                                    "achievedDate" : "2025-01-01",
                                    "achievedGoalIds" : [1]
                                }
                                  """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "생성 성공 예시",
                        value =
                            """
                                          {
                                            "message": "다이어리 생성을 성공했습니다.",
                                            "data": {
                                                 	    "id": 1,
                                                 	    "challenge": {
                                                             "challengeId": 1,
                                                             "title": "30일 코딩 챌린지",
                                                             "category": "DEV",
                                                             "startDate": "2025-09-01",
                                                             "endDate": "2025-09-30",
                                                             "maxParticipantCnt": 10,
                                                             "challengeType": "FIXED",
                                                             "participantCnt": 1,
                                                             "likeInfo": {
                                                               "likedByMe": false,
                                                               "likeCnt": 0
                                                             }
                                                        },
                                                 	    "author": {
                                                 	      "id": 1,
                                                 	      "nickname": "john",
                                                 	      "profileImage": "https://example.com/images/profile1.jpg"
                                                 	    },
                                                 	    "title": "나의 다이어리",
                                                        "content": "완수하기",
                                                        "imgUrl": null,
                                                        "isPublic": true,
                                                        "likeInfo": {
                                                          "likedByMe": false,
                                                          "likeCnt": 0
                                                        },
                                                 	    "diaryInfoDto": {
                                                            "createdAt": "2026-02-25",
                                                            "challengedDate": "2025-01-01",
                                                            "feeling": "HAPPY",
                                                            "achievement": [
                                                              1
                                                            ],
                                                            "achievementRate": 50
                                                        }
                                            }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "일지 생성 실패(인증 필요)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "인증 실패",
                        value =
                            """
                            {
                              "code": "AUTH-001",
                              "message": "일지 생성에 실패했습니다. 로그인 후 다시 시도해주세요."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일지 생성 실패(대상 없음)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                                    {
                                      "code": "USER-003",
                                      "message": "일지 생성에 실패했습니다. 사용자를 찾을 수 없습니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "챌린지 없음",
                      value =
                          """
                                    {
                                      "code": "CHALLENGE_001",
                                      "message": "일지 생성에 실패했습니다. 챌린지를 찾을 수 없습니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "참여자 없음",
                      value =
                          """
                                    {
                                      "code": "CHALLENGE_003",
                                      "message": "일지 생성에 실패했습니다. 챌린지 참여자만 일지를 작성할 수 있습니다."
                                    }
                                    """)
                }))
  })
  @PostMapping("")
  public ApiResponse<DiaryResponse> createDiary(@RequestBody DiaryRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    DiaryResponse result = diaryService.createDiary(memberId, request);
    return ApiResponse.success(Message.DIARY_CREATE_SUCCESS, result);
  }

  @Operation(summary = "일지 수정", description = "현재 로그인한 사용자의 토큰을 기반으로 일지를 수정한다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "일지 수정 요청 DTO",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = DiaryRequest.class),
              examples =
                  @ExampleObject(
                      name = "일지 수정 예시",
                      value =
                          """
                                {
                                  "challengeId": 1,
                                  "title": "오늘을 되돌아보며",
                                  "content": "알바 5시간만 해도 지쳤다",
                                  "feeling": "SAD",
                                  "isPublic": true,
                                  "achievedDate": "2025-08-02",
                                  "achievedGoalIds": [2]
                                }
                                """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 수정 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "조회 성공 예시",
                        value =
                            """
                                          {
                                            "message": "다이어리 수정을 성공했습니다.",
                                            "data": {
                                                 	    "id": 1,
                                                 	    "challenge": {
                                                             "challengeId": 1,
                                                             "title": "30일 코딩 챌린지",
                                                             "category": "DEV",
                                                             "startDate": "2025-09-01",
                                                             "endDate": "2025-09-30",
                                                             "maxParticipantCnt": 10,
                                                             "challengeType": "FIXED",
                                                             "participantCnt": 1,
                                                             "likeInfo": {
                                                               "likedByMe": false,
                                                               "likeCnt": 0
                                                             }
                                                        },
                                                 	    "author": {
                                                 	      "id": 1,
                                                 	      "nickname": "john",
                                                 	      "profileImage": "https://example.com/images/profile1.jpg"
                                                 	    },
                                                 	    "title": "오늘을 되돌아보며",
                                                        "content": "알바 5시간만 해도 지쳤다",
                                                        "imgUrl": null,
                                                        "isPublic": true,
                                                        "likeInfo": {
                                                          "likedByMe": false,
                                                          "likeCnt": 0
                                                        },
                                                 	    "diaryInfo": {
                                                            "createdAt": "2026-02-25",
                                                            "challengedDate": "2025-01-01",
                                                            "feeling": "SAD",
                                                            "achievement": [
                                                              1
                                                            ],
                                                            "achievementRate": 50
                                                        }
                                            }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "일지 생성 실패(인증 필요)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "인증 실패",
                        value =
                            """
              {
                "code": 401,
                "message": "일지 생성에 실패했습니다. 로그인 후 다시 시도해주세요.",
              }
              """)))
  })
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "400",
      description = "일지 수정 실패(요청 값 검증 실패 등)",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
              examples = {
                @ExampleObject(
                    name = "챌린지 없음",
                    value =
                        """
                                        { "code": "CHALLENGE_001", message: "챌린지를 찾을 수 없습니다." }
                                        """),
                @ExampleObject(
                    name = "참여자 아님",
                    value =
                        """
                                        {
                                          { "code": "CHALLENGE_003", message: "챌린지 참여자가 아닙니다." }
                                        }
                                        """)
              }))
  @PatchMapping("/{id}")
  public ApiResponse<DiaryResponse> updateDiary(
      @PathVariable(name = "id") Long diaryId, @RequestBody DiaryRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    DiaryResponse result = diaryService.updateDiary(memberId, diaryId, request);
    return ApiResponse.success(Message.DIARY_UPDATE_SUCCESS, result);
  }

  @Operation(summary = "일지 단일 조회", description = "일지 ID(diaryId)를 기반으로 일지 상세 정보를 조회한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 단일 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "조회 성공 예시",
                        value =
                            """
                                            {
                                              "message": "DIARY_GET_SUCCESS",
                                              "data": {
                                                 	    "id": 1,
                                                 	    "challenge": {
                                                             "challengeId": 1,
                                                             "title": "30일 코딩 챌린지",
                                                             "category": "DEV",
                                                             "startDate": "2025-09-01",
                                                             "endDate": "2025-09-30",
                                                             "maxParticipantCnt": 10,
                                                             "challengeType": "FIXED",
                                                             "participantCnt": 1,
                                                             "likeInfo": {
                                                               "likedByMe": false,
                                                               "likeCnt": 0
                                                             }
                                                        },
                                                 	    "author": {
                                                 	      "id": 1,
                                                 	      "nickname": "john",
                                                 	      "profileImage": "https://example.com/images/profile1.jpg"
                                                 	    },
                                                 	    "title": "오늘을 되돌아보며",
                                                        "content": "알바 5시간만 해도 지쳤다",
                                                        "imgUrl": null,
                                                        "isPublic": true,
                                                        "likeInfo": {
                                                          "likedByMe": false,
                                                          "likeCnt": 0
                                                        },
                                                 	    "diaryInfo": {
                                                            "createdAt": "2026-02-25",
                                                            "challengedDate": "2025-01-01",
                                                            "feeling": "SAD",
                                                            "achievement": [
                                                              1
                                                            ],
                                                            "achievementRate": 50
                                                        }
                                            }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 조회 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "잘못된 diaryId 형식",
                      value =
                          """
                                                { "code": "CHALLENGE_001", message: "챌린지를 찾을 수 없습니다." }
                                                """),
                  @ExampleObject(
                      name = "존재하지 않는 일지",
                      value =
                          """
                                                {
                                                  "message": "DIARY_NOT_FOUND",
                                                  "data": null
                                                }
                                                """)
                }))
  })
  @GetMapping("/{id}")
  public ApiResponse<DiaryResponse> getDiary(@PathVariable(name = "id") Long diaryId) {
    DiaryResponse result = diaryService.getDiary(diaryId);
    return ApiResponse.success(Message.DIARY_GET_SUCCESS, result);
  }

  @Operation(
      summary = "일지 페이지네이션으로 공개된 일지 모두 조회",
      description =
          "프론트가 지정한 한 번에 몇개의 일지를 보여줄 것인지의 size 개씩 일지를 조회한다."
              + "ex) size=10이면 size=10 & cursor=null로 보여주고 그 다음엔 cursor값과 size=10으로 다음 10개의 일지 가져옮")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 부분 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 부분 조회 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class)))
  })
  @GetMapping("")
  public ApiResponse<Pagination<DiaryResponse>> getAllDiaryBySize(
      @Parameter(
              description =
                  """
                    한 번에 조회할 개수.
                    - null 또는 원하는 수, 세팅 안 하면 기본값 10
                    - 최대 100까지 허용됨
                  """,
              example = "10")
          @RequestParam(name = "size", required = false)
          Integer size,
      @Parameter(
              description =
                  """
                        커서(다음 페이지 조회용).
                        - 첫 요청에는 생략
                        - 다음 요청부터는 응답 pageInfo.nextCursor 값을 그대로 넣어 호출
                   """,
              example = "")
          @RequestParam(name = "cursor", required = false)
          String cursor) {
    Pagination<DiaryResponse> result = diaryService.getPublicDiariesPage(size, cursor);
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @Operation(
      summary = "공개된 일지 한 번에 모두 조회 ",
      description = "생성된 모든 일지를 한번에 조회한다. (테스트용으로 만들었습니다 - 굳이 명세는 X, 위에 있는 명세 참고부탁드립니다).")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 조회 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class)))
  })
  @GetMapping("/all")
  public ApiResponse<List<DiaryResponse>> getAllDiary() {
    List<DiaryResponse> result = diaryService.getAllPublicDiaries();
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @Operation(summary = "일지 랜덤 조회", description = "size개씩 랜덤으로 일지 조회")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 랜덤 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "랜덤 조회 성공 예시",
                        value =
                            """
                                          {
                                               "message": "다이어리 랜덤 조회 성공했습니다.",
                                               "data": [
                                                   {
                                                       "id": 1,
                                                       "authorInfo": {
                                                           "id": 1,
                                                           "nickname": null,
                                                           "profileImage": null
                                                       },
                                                       "title": "꾸니 다이어리1",
                                                       "content": "content1",
                                                       "imgUrl": null,
                                                       "isPublic": true,
                                                       "likeInfo": {
                                                           "likedByMe": true,
                                                           "likeCnt": 2
                                                       },
                                                       "diaryInfo": {
                                                           "createdAt": "2025-08-23",
                                                           "challengedDate": "2025-01-01",
                                                           "feeling": "SAD",
                                                           "achievement": null,
                                                           "achievementRate": 0
                                                       }
                                                   },
                                                   {
                                                       "id": 7,
                                                       "authorInfo": {
                                                           "id": 2,
                                                           "nickname": null,
                                                           "profileImage": null
                                                       },
                                                       "title": "아리의 다이어리7",
                                                       "content": "content1",
                                                       "imgUrl": null,
                                                       "isPublic": true,
                                                       "likeInfo": {
                                                           "likedByMe": false,
                                                           "likeCnt": 0
                                                       },
                                                       "diaryInfo": {
                                                           "createdAt": "2025-08-23",
                                                           "challengedDate": "2025-01-01",
                                                           "feeling": "SAD",
                                                           "achievement": null,
                                                           "achievementRate": 0
                                                       }
                                                   },
                                                   ....
                                          }
                                          """)))
  })
  @GetMapping("/random")
  public ApiResponse<List<DiaryResponse>> randomDiary(
      @Parameter(description = "랜덤으로 가져올 일지 개수", example = "5", required = true)
          @RequestParam("size")
          Long size) {
    List<DiaryResponse> result = diaryService.getRandomDiaries(size);
    return ApiResponse.success(Message.DIARY_VIEW_RANDOM, result);
  }

  @Operation(summary = "일지 삭제", description = "일지의 id를 기반으로 일지를 삭제한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "삭제 성공 예시",
                        value =
                            """
                            {
                              "data": "",
                              "message": "일지 삭제에 성공했습니다."
                            }
                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일지 삭제 실패(일지 없음)",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "일지 없음",
                        value =
                            """
                            {
                              "code": "DIARY-002",
                              "message": "일지 삭제에 실패했습니다. 존재하지 않는 일지입니다."
                            }
                            """)))
  })
  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> deleteDiary(
      @Parameter(description = "삭제할 일지 ID", example = "1", required = true)
          @PathVariable(name = "id")
          Long diaryId) {
    Boolean result = diaryService.deleteDiary(diaryId);
    return ApiResponse.success(Message.DIARY_DELETE_SUCCESS, result);
  }

  @Operation(
      summary = "일지에 좋아요 누르기",
      description = "일지 ID 기반으로 좋아요를 생성하고, 처리 후 해당 일지의 좋아요 총 개수를 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 좋아요 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "좋아요 성공 예시",
                        value =
                            """
                                    {
                                      "message": "다이어리에 좋아요 누르기 성공했습니다.",
                                      "data": 1
                                    }
                                    """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 좋아요 누르기 실패(요청 값 검증 실패, 이미 누른 상태 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "일지 없음",
                      value =
                          """
                                    {
                                      "code": "DIARY-002",
                                      "message": "좋아요 처리에 실패했습니다. 존재하지 않는 일지입니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                                    {
                                      "code": "USER-003",
                                      "message": "좋아요 처리에 실패했습니다. 사용자를 찾을 수 없습니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "이미 좋아요 상태",
                      value =
                          """
                                    {
                                      "code": "DIARY-003",
                                      "message": "좋아요 처리에 실패했습니다. 이미 좋아요를 누른 상태입니다."
                                    }
                                    """)
                }))
  })
  @PostMapping("/{id}/likes")
  public ApiResponse<Integer> addLike(
      @Parameter(description = "좋아요를 누를 일지 ID", example = "101", required = true)
          @PathVariable(name = "id")
          Long diaryId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Integer likeCount = diaryService.addDiaryLike(memberId, diaryId);
    return ApiResponse.success(Message.DIARY_ADDED_LIKE, likeCount);
  }

  @Operation(
      summary = "일지 좋아요 취소",
      description = "좋아요가 눌려있는 상태에서 취소하면 좋아요가 삭제되고, 해당 일지의 좋아요 총 개수를 반환한다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 좋아요 취소 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "좋아요 취소 성공 예시",
                        value =
                            """
                                    {
                                      "message": "다이어리에 좋아요 누르기 취소 성공했습니다.",
                                      "data": 11
                                    }
                                    """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 좋아요 취소 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "일지 없음",
                      value =
                          """
                                    {
                                      "code": "DIARY-002",
                                      "message": "좋아요 취소에 실패했습니다. 존재하지 않는 일지입니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                                    {
                                      "code": "USER-003",
                                      "message": "좋아요 취소에 실패했습니다. 사용자를 찾을 수 없습니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "좋아요 기록 없음",
                      value =
                          """
                                    {
                                      "code": "DIARY-004",
                                      "message": "좋아요 취소에 실패했습니다. 좋아요를 누른 기록이 없습니다."
                                    }
                                    """)
                }))
  })
  @DeleteMapping("/{id}/likes")
  public ApiResponse<Integer> cancelLike(
      @Parameter(description = "좋아요를 취소할 일지 ID", example = "1", required = true)
          @PathVariable(name = "id")
          Long diaryId) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Integer likeCount = diaryService.cancelDiaryLike(memberId, diaryId);
    return ApiResponse.success(Message.DIARY_CANCELED_LIKE, likeCount);
  }

  @Operation(summary = "일지 신고하기 생성", description = "유저가 특정 일지에 신고 요청을 넣으면 신고하기가 생성된다.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "일지 신고 요청 DTO",
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ReportRequest.class),
              examples =
                  @ExampleObject(
                      name = "신고 요청 예시",
                      value =
                          """
                            {
                              "diaryId": 1,
                              "reportType": "BAD_TITLE_CONTENT",
                              "content": "광고성 내용이 포함되어 있어요"
                            }
                            """)))
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 신고하기 생성 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "신고 성공 예시",
                        value =
                            """
                                    {
                                      "message": "DIARY_REPORT_CREATED",
                                      "data": true
                                    }
                                    """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 신고하기 생성 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "일지 없음",
                      value =
                          """
                                    {
                                      "code": "DIARY-002",
                                      "message": "신고 접수에 실패했습니다. 존재하지 않는 일지입니다."
                                    }
                                    """),
                  @ExampleObject(
                      name = "회원 없음",
                      value =
                          """
                                    {
                                      "code": "USER-003",
                                      "message": "신고 접수에 실패했습니다. 사용자를 찾을 수 없습니다."
                                    }
                                    """)
                }))
  })
  @PostMapping("/report")
  public ApiResponse<Boolean> reportDiary(@RequestBody ReportRequest request) {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Boolean result = diaryService.reportDiary(request, memberId);
    return ApiResponse.success(Message.DIARY_REPORT_CREATED, result);
  }

  @Operation(
      summary = "나의 일지 조회",
      description =
          "로그인한 유저가 작성한 일지를 모두 조회한다."
              + "- Member 쪽에 나의 챌린지, 일지 조회, 나의 스트릭 정보 조회하는 API에서 쓰시면 됩니다."
              + "- 여기에서는 나의 일지 조회로 보통 일지조회와 작동 방식은 동일")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "나의 모든 일지 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "나의 일지 조회 성공 예시",
                        value =
                            """
                                        {
                                               "message": "다이어리 랜덤 조회 성공했습니다.",
                                               "data": [
                                                   {
                                                       "id": 1,
                                                       "authorInfo": {
                                                           "id": 1,
                                                           "nickname": null,
                                                           "profileImage": null
                                                       },
                                                       "title": "꾸니 다이어리1",
                                                       "content": "content1",
                                                       "imgUrl": null,
                                                       "isPublic": true,
                                                       "likeInfo": {
                                                           "likedByMe": true,
                                                           "likeCnt": 2
                                                       },
                                                       "diaryInfo": {
                                                           "createdAt": "2025-08-23",
                                                           "challengedDate": "2025-01-01",
                                                           "feeling": "SAD",
                                                           "achievement": null,
                                                           "achievementRate": 0
                                                       }
                                                   },
                                                   {
                                                       "id": 7,
                                                       "authorInfo": {
                                                           "id": 1,
                                                           "nickname": null,
                                                           "profileImage": null
                                                       },
                                                       "title": "아리의 다이어리7",
                                                       "content": "content1",
                                                       "imgUrl": null,
                                                       "isPublic": true,
                                                       "likeInfo": {
                                                           "likedByMe": false,
                                                           "likeCnt": 0
                                                       },
                                                       "diaryInfo": {
                                                           "createdAt": "2025-08-23",
                                                           "challengedDate": "2025-01-01",
                                                           "feeling": "SAD",
                                                           "achievement": null,
                                                           "achievementRate": 0
                                                       }
                                                   },
                                                   ....
                                          }
                                        """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "나의 모든 일지 조회 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "로그인 사용자 조회 불가능",
                        value =
                            """
                                    {
                                      "message": "MEMBER_NOT_FOUND",
                                      "code": AUTH-003
                                    }
                                    """)))
  })
  @GetMapping("/my")
  public ApiResponse<List<DiaryResponse>> getMyDiaries() {
    List<DiaryResponse> result = diaryService.getMyDiaries();
    return ApiResponse.success(Message.DIARY_GET_MY_ALL_SUCCESS, result);
  }

  @Operation(
      summary = "일지에 이미지 1장만 올리기",
      description =
          """
                일지 내용과 이미지를 분리 업로드하기 위한 API.
                특정 diaryId의 일지에 이미지 1장을 업로드한다.

                <프론트 요청 방식>
                - method: POST
                - content-type: multipart/form-data
                - form-data:
                  - file: (업로드할 이미지 파일 1개)

                예) POST /diaries/{id}/image
                """)
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 사진 업로드 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "업로드 성공 예시",
                        value =
                            """
                                            {
                                              "message": "DIARY_IMAGE_UPLOADED",
                                              "data": "0eb3a268-0702-45bb-9d3b-e9d36103c70b_bee.png"

                                            }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 사진 업로드 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "존재하지 않는 일지",
                      value =
                          """
                                            {
                                              "message": "DIARY_NOT_FOUND",
                                              "code": DIARY-002
                                            }
                                            """),
                }))
  })
  @PostMapping("/{id}/image")
  public ApiResponse<String> uploadImage(
      @Parameter(description = "이미지를 업로드할 일지 ID", example = "1", required = true)
          @PathVariable("id")
          Long diaryId,
      @Parameter(
              description = "업로드할 이미지 파일 (multipart/form-data의 file 필드)",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @RequestParam("file")
          MultipartFile file)
      throws IOException {
    String fileName = diaryService.uploadDiaryFile(diaryId, file);
    return ApiResponse.success(Message.DIARY_IMAGE_UPLOADED, fileName);
  }

  @Operation(
      summary = "일지 이미지 여러장 업로드",
      description =
          """
                일지 내용과 이미지를 분리 업로드하기 위한 API.
                특정 diaryId의 일지에 이미지 여러 장을 업로드한다.

                <프론트 요청 방식>
                - method: POST
                - content-type: multipart/form-data
                - form-data:
                  - file: 업로드할 이미지 파일들  ← 동일 key로 여러 번 append

                예) POST /diaries/{id}/images
                """)
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일지 이미지 업로드 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples =
                    @ExampleObject(
                        name = "다중 업로드 성공 예시",
                        value =
                            """
                                            {
                                              "message": "DIARY_IMAGES_UPLOADED",
                                              "data": [
                                                "0eb3a268-0702-45bb-9d3b-e9d36103c70b_bee.png",
                                                "0eb3a268-0702-45bb-9d3b-e9d36f19372v_bird.png"
                                              ]
                                            }
                                            """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일지 이미지 업로드 실패(요청 값 검증 실패 등)",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(implementation = com.odos.odos_server_v2.response.ApiResponse.class),
                examples = {
                  @ExampleObject(
                      name = "존재하지 않는 일지",
                      value =
                          """
                                            {
                                              "message": "DIARY_NOT_FOUND",
                                              "data": null
                                            }
                                            """),
                }))
  })
  @PostMapping("/{id}/images")
  public ApiResponse<List<String>> uploadImages(
      @Parameter(description = "이미지를 업로드할 일지 ID", example = "1", required = true)
          @PathVariable("id")
          Long diaryId,
      @Parameter(
              description = "업로드할 이미지 파일들 (multipart/form-data의 file 필드, 여러 개)",
              required = true,
              array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))
          @RequestParam("file")
          List<MultipartFile> files)
      throws IOException {
    List<String> fileList = diaryService.uploadDiaryFiles(diaryId, files);
    return ApiResponse.success(Message.DIARY_IMAGES_UPLOADED, fileList);
  }
}
