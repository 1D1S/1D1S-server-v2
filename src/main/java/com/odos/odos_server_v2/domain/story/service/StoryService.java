package com.odos.odos_server_v2.domain.story.service;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryImageRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.domain.story.dto.StoryGroupDto;
import com.odos.odos_server_v2.domain.story.dto.StoryItemDto;
import com.odos.odos_server_v2.domain.story.dto.StoryResponseDto;
import com.odos.odos_server_v2.domain.story.entity.DiaryViewLog;
import com.odos.odos_server_v2.domain.story.repository.DiaryViewLogRepository;
import com.odos.odos_server_v2.domain.story.repository.StoryRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
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
    Member currentMember = getCurrentMember();
    LocalDateTime since = LocalDateTime.now().minusHours(24);

    // 1. 최근 24시간 이내 친구들이 작성한 일지 전체 조회
    List<Diary> diaries = storyRepository.findFriendDiariesWithin24Hours(currentMember, since);

    if (diaries.isEmpty()) {
      return StoryResponseDto.builder().storyGroups(Collections.emptyList()).unreadCount(0).build();
    }

    // 2. 시청 기록 조회
    List<DiaryViewLog> viewLogs =
        diaryViewLogRepository.findByMemberAndDiaryIn(currentMember, diaries);
    Set<Long> viewedDiaryIds =
        viewLogs.stream().map(log -> log.getDiary().getId()).collect(Collectors.toSet());

    // 3. 친구별로 일지 그룹화
    Map<Member, List<Diary>> diariesByFriend =
        diaries.stream().collect(Collectors.groupingBy(Diary::getMember));

    // 4. 각 친구 그룹을 StoryGroupDto로 변환 및 정렬
    List<StoryGroupDto> storyGroups = new ArrayList<>();
    int totalUnreadCount = 0;

    for (Map.Entry<Member, List<Diary>> entry : diariesByFriend.entrySet()) {
      Member friendMember = entry.getKey();
      List<Diary> friendDiaries = entry.getValue();

      // 그룹 내 스토리 아이템 생성 및 정렬 (최신순, 미시청 우선)
      List<StoryItemDto> storyItems =
          friendDiaries.stream()
              .map(
                  diary -> {
                    boolean hasUnread = !viewedDiaryIds.contains(diary.getId());
                    String diaryThumbnail = diaryImageRepository.getDiaryThumbNail(diary.getId());
                    return StoryItemDto.builder()
                        .diaryId(diary.getId())
                        .diaryTitle(diary.getTitle())
                        .diaryThumbnail(diaryThumbnail)
                        .createdAt(diary.getCreatedAt())
                        .hasUnreadJournal(hasUnread)
                        .build();
                  })
              .sorted(
                  (a, b) -> {
                    // 미시청 우선, 그 다음 최신순 (createdAt 최신이 앞)
                    if (a.getHasUnreadJournal() && !b.getHasUnreadJournal()) {
                      return -1;
                    }
                    if (!a.getHasUnreadJournal() && b.getHasUnreadJournal()) {
                      return 1;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt()); // 최신순
                  })
              .collect(Collectors.toList());

      // 그룹에 미시청 스토리가 있는지 확인
      boolean hasUnreadInGroup = storyItems.stream().anyMatch(StoryItemDto::getHasUnreadJournal);
      if (hasUnreadInGroup) {
        totalUnreadCount +=
            storyItems.stream().mapToInt(item -> item.getHasUnreadJournal() ? 1 : 0).sum();
      }

      StoryGroupDto group =
          StoryGroupDto.builder()
              .userId(friendMember.getId())
              .userName(friendMember.getNickname())
              .profileImage(imageService.getFileUrl(friendMember.getProfileUrl()))
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
    Member currentMember = getCurrentMember();

    Diary diary =
        storyRepository
            .findById(diaryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));

    // 이미 시청했는지 확인
    if (!diaryViewLogRepository.existsByMemberAndDiary(currentMember, diary)) {
      DiaryViewLog viewLog = DiaryViewLog.builder().member(currentMember).diary(diary).build();
      diaryViewLogRepository.save(viewLog);
    }
  }

  private Member getCurrentMember() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
  }
}
