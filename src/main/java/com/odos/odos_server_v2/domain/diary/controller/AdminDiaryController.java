package com.odos.odos_server_v2.domain.diary.controller;

import com.odos.odos_server_v2.domain.diary.dto.DiaryDeleteAdminRequest;
import com.odos.odos_server_v2.domain.diary.dto.DiaryDeleteAdminResponse;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.diary.service.AdminDiaryService;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.ErrorResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/diaries")
@Slf4j
@Tag(name = "Admin Diary API", description = "관리자 일지 API")
public class AdminDiaryController {
  private final AdminDiaryService diaryService;

  @Operation(summary = "관리자 페이지 일지 목록 조회", description = "작성자, 챌린지, 최신순/좋아요순 필터를 적용해 일지를 조회합니다.")
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
                                                   "message": "다이어리 모든 조회를 성공했습니다.",
                                                   "data": {
                                                     "items": [
                                                       {
                                                         "id": 2,
                                                         "challenge": {
                                                           "challengeId": 1,
                                                           "title": "매일 독서하기",
                                                           "thumbnailImage": "https://odos-pic",
                                                           "category": "BOOK",
                                                           "startDate": "2026-05-20",
                                                           "endDate": "9999-12-31",
                                                           "participationType": "GROUP",
                                                           "maxParticipantCnt": null,
                                                           "goalType": "FIXED",
                                                           "challengeType": "PUBLIC",
                                                           "participantCnt": 3,
                                                           "likeInfo": {
                                                             "likedByMe": false,
                                                             "likeCnt": 2
                                                           },
                                                           "randomParticipants": [
                                                             {
                                                               "memberId": 1,
                                                               "nickname": "노아",
                                                               "profileImg": "https://odos-pic"
                                                             },
                                                             {
                                                               "memberId": 2,
                                                               "nickname": "주나",
                                                               "profileImg": null
                                                             },
                                                             {
                                                               "memberId": 3,
                                                               "nickname": "코코",
                                                               "profileImg": "https://odos-pic"
                                                             }
                                                           ],
                                                           "deleted": false
                                                         },
                                                         "author": {
                                                           "id": 1,
                                                           "nickname": "코코몽",
                                                           "profileImage": "https://odos-pic"
                                                         },
                                                         "title": "넥OO 20p",
                                                         "content": "책을 읽자",
                                                         "imgUrl": [],
                                                         "isPublic": true,
                                                         "likeInfo": {
                                                           "likedByMe": false,
                                                           "likeCnt": 0
                                                         },
                                                         "commentCount": 0,
                                                         "diaryInfo": {
                                                           "createdAt": "2026-06-11T18:43:04",
                                                           "challengedDate": "2026-06-11",
                                                           "feeling": "NORMAL",
                                                           "diaryGoal": [
                                                             {
                                                               "challengeGoalId": 1,
                                                               "challengeGoalName": "책 읽기",
                                                               "isAchieved": true
                                                             }
                                                           ],
                                                           "achievementRate": 100
                                                         }
                                                       },
                                                       {
                                                         "id": 1,
                                                         "challenge": {
                                                           "challengeId": 1,
                                                           "title": "1일 1코테",
                                                           "thumbnailImage": "https://odos-pic",
                                                           "category": "DEV",
                                                           "startDate": "2026-05-25",
                                                           "endDate": "2026-06-23",
                                                           "participationType": "GROUP",
                                                           "maxParticipantCnt": null,
                                                           "goalType": "FIXED",
                                                           "challengeType": "PUBLIC",
                                                           "participantCnt": 2,
                                                           "likeInfo": {
                                                             "likedByMe": false,
                                                             "likeCnt": 1
                                                           },
                                                           "randomParticipants": [
                                                             {
                                                               "memberId": 4,
                                                               "nickname": "흠나리",
                                                               "profileImg": "https://odos-pic"
                                                             },
                                                             {
                                                               "memberId": 5,
                                                               "nickname": "홍길동",
                                                               "profileImg": "https://odos-pic"
                                                             }
                                                           ],
                                                           "deleted": false
                                                         },
                                                         "author": {
                                                           "id": 6,
                                                           "nickname": "승승",
                                                           "profileImage": "https://odos-pic"
                                                         },
                                                         "title": "프로그래머스 전력망 나누기",
                                                         "content": "프로그래머스 문제 의논",
                                                         "imgUrl": [],
                                                         "isPublic": true,
                                                         "likeInfo": {
                                                           "likedByMe": false,
                                                           "likeCnt": 2
                                                         },
                                                         "commentCount": 2,
                                                         "diaryInfo": {
                                                           "createdAt": "2026-06-11T16:50:28",
                                                           "challengedDate": "2026-06-11",
                                                           "feeling": "HAPPY",
                                                           "diaryGoal": [
                                                             {
                                                               "challengeGoalId": 6,
                                                               "challengeGoalName": "1일 1코테",
                                                               "isAchieved": true
                                                             }
                                                           ],
                                                           "achievementRate": 100
                                                         }
                                                       }
                                                     ],
                                                     "pageInfo": {
                                                       "page": 0,
                                                       "size": 2,
                                                       "totalElements": 219,
                                                       "totalPages": 110,
                                                       "hasNextPage": true
                                                     }
                                                   }
                                                 }
                                                    """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "인증되지 않은 접근",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "잘못된 challengeId 형식",
                      value =
                          """
                                                    {
                                                      "code": "DIARY-006",
                                                      "message": "관리자 다이어리 필터 요청 형식이 올바르지 않습니다."
                                                    }
                                                    """)
                }))
  })
  @GetMapping("")
  public ApiResponse<OffsetPagination<DiaryResponse>> getAllDiaryByOffset(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @Parameter(
              description =
                  "필터 타입 (최신순: latest, 좋아요순: likes, 챌린지: challenge, 닉네임: author)"
                      + "챌린지와 닉네임을 사용할때는 챌린지값과 닉네임을 동시에 파라미터에 넣으면 에러가 터집니다. "
                      + "ex) /admin/diaries?page=0&size=2&filterType=author&authorNickname=닉으로만 가능",
              example = "latest")
          @RequestParam(name = "filterType", defaultValue = "latest")
          String filterType,
      @Parameter(description = "filterType이 author일 때 해당 닉네임", example = "코코")
          @RequestParam(name = "authorNickname", required = false)
          String authorNickname,
      @Parameter(description = "챌린지 필터일때 해당 챌린지의 id", example = "1")
          @RequestParam(name = "challengeId", required = false)
          Long challengeId) {
    Pageable pageable = PageRequest.of(page, size);
    OffsetPagination<DiaryResponse> result =
        diaryService.getDiariesByAdmin(pageable, filterType, authorNickname, challengeId);
    return ApiResponse.success(Message.DIARY_GET_ALL_SUCCESS, result);
  }

  @Operation(summary = "관리자 일지 상세 조회", description = "일지 ID를 기반으로 일지 상세 정보를 조회합니다.")
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
                                                 "message": "다이어리 단일조회를 성공했습니다.",
                                                 "data": {
                                                   "id": 1,
                                                   "challenge": {
                                                     "challengeId": 1,
                                                     "title": "1D1S 베타 챌린지",
                                                     "thumbnailImage": null,
                                                     "category": "DEV",
                                                     "startDate": "2026-03-16",
                                                     "endDate": "2026-03-29",
                                                     "participationType": "GROUP",
                                                     "maxParticipantCnt": 50,
                                                     "goalType": "FLEXIBLE",
                                                     "challengeType": "PUBLIC",
                                                     "participantCnt": 6,
                                                     "likeInfo": {
                                                       "likedByMe": true,
                                                       "likeCnt": 7
                                                     },
                                                     "randomParticipants": [
                                                       {
                                                         "memberId": 1,
                                                         "nickname": "호시",
                                                         "profileImg": null
                                                       },
                                                       {
                                                         "memberId": 2,
                                                         "nickname": "두리",
                                                         "profileImg": null
                                                       },
                                                       {
                                                         "memberId": 3,
                                                         "nickname": "드기",
                                                         "profileImg": null
                                                       }
                                                     ],
                                                     "deleted": false
                                                   },
                                                   "author": {
                                                     "id": 1,
                                                     "nickname": "나나",
                                                     "profileImage": "https://odos-pic"
                                                   },
                                                   "title": "환영합니다",
                                                   "content": "같이해요",
                                                   "imgUrl": [
                                                     "picture"
                                                   ],
                                                   "isPublic": true,
                                                   "likeInfo": {
                                                     "likedByMe": true,
                                                     "likeCnt": 5
                                                   },
                                                   "commentCount": 0,
                                                   "diaryInfo": {
                                                     "createdAt": "2026-03-16T21:26:49",
                                                     "challengedDate": "2026-03-16",
                                                     "feeling": "HAPPY",
                                                     "diaryGoal": [
                                                       {
                                                         "challengeGoalId": 1,
                                                         "challengeGoalName": "버그 수정!",
                                                         "isAchieved": true
                                                       },
                                                       {
                                                         "challengeGoalId": 2,
                                                         "challengeGoalName": "피드백 반영!",
                                                         "isAchieved": true
                                                       }
                                                     ],
                                                     "achievementRate": 100
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
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "관리자가 아닌 경우",
                      value =
                          """
                                                  {
                                                    "code": "AUTH-001",
                                                    "message": "인증되지 않은 접근입니다."
                                                  }
                                                  """)
                })),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "해당 일지가 없음",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "해당 일지가 없을 때",
                      value =
                          """
                                                  {
                                                    "code": "DIARY-002",
                                                    "message": "존재하지 않는 다이어리입니다."
                                                  }
                                                  """)
                }))
  })
  @GetMapping("/{id}")
  public ApiResponse<DiaryResponse> getDiary(@PathVariable(name = "id") Long diaryId) {
    DiaryResponse result = diaryService.getDiaryByAdmin(diaryId);
    return ApiResponse.success(Message.DIARY_GET_SUCCESS, result);
  }

  @Operation(summary = "관리자 일지 삭제", description = "관리자가 부적절한 일지를 삭제 사유와 함께 삭제 처리합니다.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "관리자 일지 삭제 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        value =
                            """
                                        {
                                          "message": "일지 삭제 성공했습니다.",
                                          "data": {
                                            "diaryId": 1,
                                            "diaryTitle": "오늘의 운동 기록",
                                            "diaryDeletedReason": "부적절한 내용이 포함되어 있습니다."
                                          }
                                        }
                                        """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "삭제 사유 누락",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          {
                                            "code": "DIARY-007",
                                            "message": "일지 삭제 사유를 입력해야 합니다."
                                          }
                                          """))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 일지",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                                          {
                                            "code": "DIARY-002",
                                            "message": "존재하지 않는 다이어리입니다."
                                          }
                                          """)))
  })
  @PatchMapping("")
  public ApiResponse<DiaryDeleteAdminResponse> deleteDiary(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "관리자 일지 삭제 요청",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = DiaryDeleteAdminRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                        {
                                          "diaryId": 1,
                                          "deleteReason": "부적절한 내용이 포함되어 있습니다."
                                        }
                                        """)))
          @RequestBody
          DiaryDeleteAdminRequest request) {
    DiaryDeleteAdminResponse result = diaryService.deleteDiaryByAdmin(request);
    return ApiResponse.success(Message.DIARY_DELETE_SUCCESS, result);
  }
}
