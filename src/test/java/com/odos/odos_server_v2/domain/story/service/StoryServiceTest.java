package com.odos.odos_server_v2.domain.story.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.repository.DiaryImageRepository;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.shared.service.ImageService;
import com.odos.odos_server_v2.domain.story.dto.StoryDiarySummaryProjection;
import com.odos.odos_server_v2.domain.story.dto.StoryResponseDto;
import com.odos.odos_server_v2.domain.story.entity.DiaryViewLog;
import com.odos.odos_server_v2.domain.story.repository.DiaryViewLogRepository;
import com.odos.odos_server_v2.domain.story.repository.StoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoryServiceTest {

  private static final Long CURRENT_MEMBER_ID = 1L;

  private StoryRepository storyRepository;
  private DiaryViewLogRepository diaryViewLogRepository;
  private DiaryImageRepository diaryImageRepository;
  private ImageService imageService;
  private StoryService storyService;

  @BeforeEach
  void setUp() {
    storyRepository = mock(StoryRepository.class);
    diaryViewLogRepository = mock(DiaryViewLogRepository.class);
    diaryImageRepository = mock(DiaryImageRepository.class);
    imageService = mock(ImageService.class);

    storyService =
        new StoryService(
            storyRepository,
            diaryViewLogRepository,
            mock(MemberRepository.class),
            diaryImageRepository,
            imageService);

    MemberPrincipal principal =
        new MemberPrincipal(CURRENT_MEMBER_ID, "me@example.com", "USER", SignupRoute.KAKAO);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void sortsStoriesAndPreservesRepositoryOrderForGroupsWithSamePriority() {
    StoryDiarySummaryProjection viewedFriendStory =
        story(20L, 2L, "친구", LocalDateTime.of(2026, 6, 30, 9, 0));
    StoryDiarySummaryProjection olderUnreadFriendStory =
        story(21L, 2L, "친구", LocalDateTime.of(2026, 6, 30, 10, 0));
    StoryDiarySummaryProjection newerUnreadFriendStory =
        story(22L, 2L, "친구", LocalDateTime.of(2026, 6, 30, 12, 0));
    StoryDiarySummaryProjection anotherUnreadFriendStory =
        story(30L, 32L, "다른 친구", LocalDateTime.of(2026, 6, 30, 11, 30));
    StoryDiarySummaryProjection olderMyStory =
        story(10L, CURRENT_MEMBER_ID, "나", LocalDateTime.of(2026, 6, 30, 11, 0));
    StoryDiarySummaryProjection newerMyStory =
        story(11L, CURRENT_MEMBER_ID, "나", LocalDateTime.of(2026, 6, 30, 13, 0));
    DiaryViewLog viewedFriendStoryLog = viewLog(20L);

    when(storyRepository.findStoryDiarySummariesWithin24Hours(
            eq(CURRENT_MEMBER_ID), any(LocalDateTime.class)))
        .thenReturn(
            List.of(
                newerMyStory,
                newerUnreadFriendStory,
                anotherUnreadFriendStory,
                olderMyStory,
                olderUnreadFriendStory,
                viewedFriendStory));
    when(diaryViewLogRepository.findByMemberIdAndDiaryIdIn(
            CURRENT_MEMBER_ID, List.of(11L, 22L, 30L, 10L, 21L, 20L)))
        .thenReturn(List.of(viewedFriendStoryLog));
    when(diaryImageRepository.getDiaryThumbNail(any(Long.class))).thenReturn("thumbnail");
    when(imageService.getFileUrl(any())).thenReturn("profile-url");

    StoryResponseDto response = storyService.getStories();

    assertThat(response.getStoryGroups())
        .extracting(group -> group.getUserId())
        .containsExactly(CURRENT_MEMBER_ID, 2L, 32L);
    assertThat(response.getStoryGroups())
        .extracting(group -> group.getIsMyStory())
        .containsExactly(true, false, false);
    assertThat(response.getStoryGroups().get(0).getStories())
        .extracting(story -> story.getDiaryId())
        .containsExactly(10L, 11L);
    assertThat(response.getStoryGroups().get(0).getStories())
        .allMatch(story -> !story.getHasUnreadJournal());
    assertThat(response.getStoryGroups().get(1).getStories())
        .extracting(story -> story.getDiaryId())
        .containsExactly(21L, 22L, 20L);
    assertThat(response.getUnreadCount()).isEqualTo(3);
  }

  private StoryDiarySummaryProjection story(
      Long diaryId, Long memberId, String nickname, LocalDateTime createdAt) {
    StoryDiarySummaryProjection story = mock(StoryDiarySummaryProjection.class);
    when(story.getDiaryId()).thenReturn(diaryId);
    when(story.getDiaryTitle()).thenReturn("일지 " + diaryId);
    when(story.getCreatedAt()).thenReturn(createdAt);
    when(story.getMemberId()).thenReturn(memberId);
    when(story.getMemberNickname()).thenReturn(nickname);
    when(story.getMemberProfileUrl()).thenReturn("profile");
    return story;
  }

  private DiaryViewLog viewLog(Long diaryId) {
    Diary diary = mock(Diary.class);
    when(diary.getId()).thenReturn(diaryId);

    DiaryViewLog viewLog = mock(DiaryViewLog.class);
    when(viewLog.getDiary()).thenReturn(diary);
    return viewLog;
  }
}
