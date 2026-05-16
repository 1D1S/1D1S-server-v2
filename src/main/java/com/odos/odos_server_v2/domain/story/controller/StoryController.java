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

  @Operation(summary = "스토리 목록 조회", description = "최근 24시간 이내 친구들이 작성한 일지 목록을 조회합니다.")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "스토리 목록 조회 성공",
        content =
            @Content(
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      value =
                          """
                    {
                      "message": "스토리 목록 조회 성공했습니다.",
                      "data": {
                        "storyGroups": [
                          {
                            "userId": 2,
                            "profileImage": "https://example.com/profile.jpg",
                            "stories": [
                              {
                                "diaryId": 10,
                                "diaryTitle": "나의 다이어리",
                                "diaryThumbnail": "12345image.webp",
                                "createdAt": "2024-01-01T12:00:00",
                                "hasUnreadJournal": true
                              },
                              {
                                "diaryId": 8,
                                "diaryTitle": "나의 다이어리",
                                "diaryThumbnail": "12345.webp",
                                "createdAt": "2024-01-01T10:00:00",
                                "hasUnreadJournal": false
                              }
                            ]
                          }
                        ],
                        "unreadCount": 3
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
