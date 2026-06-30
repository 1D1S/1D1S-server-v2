package com.odos.odos_server_v2.domain.story.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import com.odos.odos_server_v2.domain.story.dto.StoryResponseDto;
import com.odos.odos_server_v2.domain.story.service.StoryService;
import com.odos.odos_server_v2.response.ApiResponse;
import com.odos.odos_server_v2.response.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "스토리", description = "실시간 일지(스토리) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stories")
public class StoryController {

  private final StoryService storyService;

  @Operation(
      summary = "스토리 목록 조회",
      description =
          """
          최근 24시간 이내 본인과 친구들이 작성한 일지를 작성자별로 조회합니다.

          - 본인 스토리 그룹(`isMyStory=true`)은 항상 첫 번째로 반환됩니다.
          - 본인 스토리는 오래된순으로 반환됩니다.
          - 친구 스토리는 미시청 스토리가 먼저, 각 시청 상태 안에서는 오래된순으로 반환됩니다.
          - 본인 스토리가 없으면 `isMyStory=true`인 그룹은 포함되지 않습니다.
          - `unreadCount`는 친구의 미시청 스토리 수이며 본인 스토리는 제외됩니다.
          """)
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "스토리 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      name = "본인 스토리가 있는 경우",
                      value =
                          """
                    {
                      "message": "스토리 목록 조회 성공했습니다.",
                      "data": {
                        "storyGroups": [
                          {
                            "userId": 1,
                            "userName": "나",
                            "profileImage": "https://example.com/my-profile.jpg",
                            "isMyStory": true,
                            "stories": [
                              {
                                "diaryId": 1,
                                "diaryTitle": "아침 일지",
                                "diaryThumbnail": "morning.webp",
                                "createdAt": "2026-06-30T09:00:00",
                                "hasUnreadJournal": false
                              },
                              {
                                "diaryId": 2,
                                "diaryTitle": "점심 일지",
                                "diaryThumbnail": "lunch.webp",
                                "createdAt": "2026-06-30T12:00:00",
                                "hasUnreadJournal": false
                              }
                            ]
                          },
                          {
                            "userId": 2,
                            "userName": "김나영",
                            "profileImage": "https://example.com/profile.jpg",
                            "isMyStory": false,
                            "stories": [
                              {
                                "diaryId": 10,
                                "diaryTitle": "친구의 첫 번째 미시청 일지",
                                "diaryThumbnail": "friend-1.webp",
                                "createdAt": "2026-06-30T08:00:00",
                                "hasUnreadJournal": true
                              },
                              {
                                "diaryId": 11,
                                "diaryTitle": "친구의 두 번째 미시청 일지",
                                "diaryThumbnail": "friend-2.webp",
                                "createdAt": "2026-06-30T10:00:00",
                                "hasUnreadJournal": true
                              },
                              {
                                "diaryId": 8,
                                "diaryTitle": "친구의 시청한 일지",
                                "diaryThumbnail": "friend-viewed.webp",
                                "createdAt": "2026-06-30T07:00:00",
                                "hasUnreadJournal": false
                              }
                            ]
                          }
                        ],
                        "unreadCount": 2
                      }
                    }
                    """),
                  @ExampleObject(
                      name = "본인 스토리가 없는 경우",
                      value =
                          """
                    {
                      "message": "스토리 목록 조회 성공했습니다.",
                      "data": {
                        "storyGroups": [
                          {
                            "userId": 2,
                            "userName": "김나영",
                            "profileImage": "https://example.com/profile.jpg",
                            "isMyStory": false,
                            "stories": [
                              {
                                "diaryId": 10,
                                "diaryTitle": "친구의 일지",
                                "diaryThumbnail": "friend.webp",
                                "createdAt": "2026-06-30T08:00:00",
                                "hasUnreadJournal": true
                              }
                            ]
                          }
                        ],
                        "unreadCount": 1
                      }
                    }
                    """)
                }))
  })
  @GetMapping
  public ApiResponse<StoryResponseDto> getStories() {
    return ApiResponse.success(Message.GET_STORY_LIST, storyService.getStories());
  }

  @Operation(summary = "스토리 시청", description = "특정 일지를 시청합니다. (시청 기록 자동 저장)")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "스토리 시청 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {@ExampleObject(value = "{ \"message\": \"스토리를 시청했습니다.\" }")}))
  })
  @PostMapping("/view/{diaryId}")
  public ApiResponse<Message> viewStory(@PathVariable Long diaryId) {
    storyService.viewStory(diaryId);
    return ApiResponse.success(Message.STORY_VIEW);
  }
}
