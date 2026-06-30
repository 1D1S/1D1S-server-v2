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

  /** 실시간 일지(스토리) 목록 조회 - 24시간 이내 본인과 친구의 일지 조회 - 작성자별 그룹화 - 본인 그룹을 가장 먼저 배치 */
  @Transactional(readOnly = true)
  public StoryResponseDto getStories() {
    Long currentMemberId = getCurrentMemberId();
    List<StoryDiarySummaryProjection> diaries =
        storyRepository.findStoryDiarySummariesWithin24Hours(
            currentMemberId, LocalDateTime.now().minusHours(24));

    if (diaries.isEmpty()) {
      return emptyStoryResponse();
    }

    Set<Long> viewedDiaryIds = findViewedDiaryIds(currentMemberId, diaries);
    List<StoryGroupDto> storyGroups =
        createStoryGroups(currentMemberId, diaries, viewedDiaryIds);
    storyGroups.sort(this::compareStoryGroups);

    return StoryResponseDto.builder()
        .storyGroups(storyGroups)
        .unreadCount(countUnreadStories(storyGroups))
        .build();
  }

  private StoryResponseDto emptyStoryResponse() {
    return StoryResponseDto.builder()
        .storyGroups(Collections.emptyList())
        .unreadCount(0)
        .build();
  }

  private Set<Long> findViewedDiaryIds(
      Long currentMemberId, List<StoryDiarySummaryProjection> diaries) {
    List<Long> diaryIds = diaries.stream().map(StoryDiarySummaryProjection::getDiaryId).toList();

    return diaryViewLogRepository.findByMemberIdAndDiaryIdIn(currentMemberId, diaryIds).stream()
        .map(log -> log.getDiary().getId())
        .collect(Collectors.toSet());
  }

  private List<StoryGroupDto> createStoryGroups(
      Long currentMemberId,
      List<StoryDiarySummaryProjection> diaries,
      Set<Long> viewedDiaryIds) {
    Map<Long, List<StoryDiarySummaryProjection>> diariesByMember =
        diaries.stream().collect(Collectors.groupingBy(StoryDiarySummaryProjection::getMemberId));

    List<StoryGroupDto> storyGroups = new ArrayList<>();
    for (Map.Entry<Long, List<StoryDiarySummaryProjection>> entry : diariesByMember.entrySet()) {
      Long storyMemberId = entry.getKey();
      boolean isMyStory = storyMemberId.equals(currentMemberId);
      List<StoryDiarySummaryProjection> memberDiaries = entry.getValue();

      storyGroups.add(
          StoryGroupDto.builder()
              .userId(storyMemberId)
              .userName(memberDiaries.get(0).getMemberNickname())
              .profileImage(imageService.getFileUrl(memberDiaries.get(0).getMemberProfileUrl()))
              .isMyStory(isMyStory)
              .stories(createStoryItems(memberDiaries, isMyStory, viewedDiaryIds))
              .build());
    }
    return storyGroups;
  }

  private List<StoryItemDto> createStoryItems(
      List<StoryDiarySummaryProjection> diaries,
      boolean isMyStory,
      Set<Long> viewedDiaryIds) {
    return diaries.stream()
        .map(diary -> createStoryItem(diary, isMyStory, viewedDiaryIds))
        .sorted((a, b) -> compareStoryItems(a, b, isMyStory))
        .collect(Collectors.toList());
  }

  private StoryItemDto createStoryItem(
      StoryDiarySummaryProjection diary, boolean isMyStory, Set<Long> viewedDiaryIds) {
    return StoryItemDto.builder()
        .diaryId(diary.getDiaryId())
        .diaryTitle(diary.getDiaryTitle())
        .diaryThumbnail(diaryImageRepository.getDiaryThumbNail(diary.getDiaryId()))
        .createdAt(diary.getCreatedAt())
        .hasUnreadJournal(!isMyStory && !viewedDiaryIds.contains(diary.getDiaryId()))
        .build();
  }

  private int compareStoryItems(StoryItemDto a, StoryItemDto b, boolean isMyStory) {
    if (!isMyStory) {
      int unreadComparison =
          Boolean.compare(b.getHasUnreadJournal(), a.getHasUnreadJournal());
      if (unreadComparison != 0) {
        return unreadComparison;
      }
    }
    return a.getCreatedAt().compareTo(b.getCreatedAt());
  }

  private int compareStoryGroups(StoryGroupDto a, StoryGroupDto b) {
    int myStoryComparison =
        Boolean.compare(Boolean.TRUE.equals(b.getIsMyStory()), Boolean.TRUE.equals(a.getIsMyStory()));
    if (myStoryComparison != 0) {
      return myStoryComparison;
    }
    return Boolean.compare(hasUnreadStory(b), hasUnreadStory(a));
  }

  private boolean hasUnreadStory(StoryGroupDto group) {
    return group.getStories().stream().anyMatch(StoryItemDto::getHasUnreadJournal);
  }

  private int countUnreadStories(List<StoryGroupDto> storyGroups) {
    return (int)
        storyGroups.stream()
            .flatMap(group -> group.getStories().stream())
            .filter(StoryItemDto::getHasUnreadJournal)
            .count();
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
