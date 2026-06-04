package com.odos.odos_server_v2.domain.story.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.odos.odos_server_v2.domain.diary.repository.DiaryImageRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.domain.story.dto.StoryDiarySummaryProjection;
import com.odos.odos_server_v2.domain.story.dto.StoryGroupDto;
import com.odos.odos_server_v2.domain.story.dto.StoryItemDto;
import com.odos.odos_server_v2.domain.story.dto.StoryResponseDto;
import com.odos.odos_server_v2.domain.story.entity.DiaryViewLog;
import com.odos.odos_server_v2.domain.story.repository.DiaryViewLogRepository;
import com.odos.odos_server_v2.domain.story.repository.StoryRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class StoryService {

  private final StoryRepository storyRepository;
  private final DiaryViewLogRepository diaryViewLogRepository;
  private final MemberRepository memberRepository;
  private final DiaryImageRepository diaryImageRepository;
  private final ImageService imageService;

  /** 실시간 일지(스토리) 목록 조회 - 24시간 이내 친구의 일지 조회 - 인스타그램처럼 친구별로 그룹화하여 반환 - 미시청 스토리가 있는 그룹 우선 배치 */
  @Transactional(readOnly = true)
  public StoryResponseDto getStories() {
    Long currentMemberId = getCurrentMemberId();
    LocalDateTime since = LocalDateTime.now().minusHours(24);

    // 1. 최근 24시간 이내 친구들이 작성한 일지 요약 조회
    List<StoryDiarySummaryProjection> diaries =
        storyRepository.findFriendDiarySummariesWithin24Hours(currentMemberId, since);

    if (diaries.isEmpty()) {
      return StoryResponseDto.builder().storyGroups(Collections.emptyList()).unreadCount(0).build();
    }

    // 2. 시청 기록 조회
    List<Long> diaryIds = diaries.stream().map(StoryDiarySummaryProjection::getDiaryId).toList();
    List<DiaryViewLog> viewLogs =
        diaryViewLogRepository.findByMemberIdAndDiaryIdIn(currentMemberId, diaryIds);
    Set<Long> viewedDiaryIds =
        viewLogs.stream().map(log -> log.getDiary().getId()).collect(Collectors.toSet());

    // 3. 친구별로 일지 그룹화
    Map<Long, List<StoryDiarySummaryProjection>> diariesByFriend =
        diaries.stream().collect(Collectors.groupingBy(StoryDiarySummaryProjection::getMemberId));

    // 4. 각 친구 그룹을 StoryGroupDto로 변환 및 정렬
    List<StoryGroupDto> storyGroups = new ArrayList<>();
    int totalUnreadCount = 0;

    for (Map.Entry<Long, List<StoryDiarySummaryProjection>> entry : diariesByFriend.entrySet()) {
      Long friendMemberId = entry.getKey();
      List<StoryDiarySummaryProjection> friendDiaries = entry.getValue();
      StoryDiarySummaryProjection firstDiary = friendDiaries.get(0);

      // 그룹 내 스토리 아이템 생성 및 정렬 (미시청 우선, 최신순)
      List<StoryItemDto> storyItems =
          friendDiaries.stream()
              .map(
                  diary -> {
                    boolean hasUnread = !viewedDiaryIds.contains(diary.getDiaryId());
                    String diaryThumbnail = diaryImageRepository.getDiaryThumbNail(diary.getDiaryId());
                    return StoryItemDto.builder()
                        .diaryId(diary.getDiaryId())
                        .diaryTitle(diary.getDiaryTitle())
                        .diaryThumbnail(diaryThumbnail)
                        .createdAt(diary.getCreatedAt())
                        .hasUnreadJournal(hasUnread)
                        .build();
                  })
              .sorted(
                  (a, b) -> {
                    if (a.getHasUnreadJournal() && !b.getHasUnreadJournal()) {
                      return -1;
                    }
                    if (!a.getHasUnreadJournal() && b.getHasUnreadJournal()) {
                      return 1;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                  })
              .collect(Collectors.toList());

      int unreadCountInGroup =
          (int) storyItems.stream().filter(StoryItemDto::getHasUnreadJournal).count();
      totalUnreadCount += unreadCountInGroup;

      StoryGroupDto group =
          StoryGroupDto.builder()
              .userId(friendMemberId)
              .userName(firstDiary.getMemberNickname())
              .profileImage(imageService.getFileUrl(firstDiary.getMemberProfileUrl()))
              .stories(storyItems)
              .build();

      storyGroups.add(group);
    }

    // 5. 그룹 정렬: 미시청 스토리가 있는 그룹 우선
    storyGroups.sort(
        (a, b) -> {
          boolean aHasUnread = a.getStories().stream().anyMatch(StoryItemDto::getHasUnreadJournal);
          boolean bHasUnread = b.getStories().stream().anyMatch(StoryItemDto::getHasUnreadJournal);

          if (aHasUnread && !bHasUnread) {
            return -1;
          }
          if (!aHasUnread && bHasUnread) {
            return 1;
          }
          return 0; // 같은 경우 순서 유지
        });

    return StoryResponseDto.builder()
        .storyGroups(storyGroups)
        .unreadCount(totalUnreadCount)
        .build();
  }

  /** 스토리 시청 (일지 조회 시 자동 기록) */
  @Transactional
  public void viewStory(Long diaryId) {
    Long currentMemberId = getCurrentMemberId();

    if (!storyRepository.existsById(diaryId)) {
      throw new CustomException(ErrorCode.DIARY_NOT_FOUND);
    }

    if (!diaryViewLogRepository.existsByMemberIdAndDiaryId(currentMemberId, diaryId)) {
      DiaryViewLog viewLog =
          DiaryViewLog.builder()
              .member(memberRepository.getReferenceById(currentMemberId))
              .diary(storyRepository.getReferenceById(diaryId))
              .build();
      diaryViewLogRepository.save(viewLog);
    }
  }

  private Long getCurrentMemberId() {
    return CurrentUserContext.getCurrentMemberId();
  }
}
