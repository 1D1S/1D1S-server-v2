package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.member.entity.Member;
import java.util.List;
import lombok.Builder;

@Builder
public class DiaryResponse {
  private Long id;
  // private ChallengeResponse challenge;
  private AuthorInfo authorInfo;
  private String title;
  private String content;
  private List<String> imgUrl;
  private Boolean isPublic;
  private Boolean likedByMe;
  private int likeCount;
  private DiaryInfo diaryInfo;

  public static DiaryResponse from(Member member, Diary diary) {
    //      List<Long> achievements = diary.getDiaryGoals() == null
    //              ? Collections.emptyList()
    //              : diary.getDiaryGoals().stream()
    //              .map(DiaryGoal::getId)
    //              .toList();
    //
    //      int totalGoal = diary.getDiaryGoals().size();
    //      long achievedGalsCount =
    //        diary.getDiaryGoals().stream().filter(DiaryGoal::getIsCompleted).count();
    //      int achievementRate = (int) (achievedGalsCount * 100) / totalGoal;

    //    DiaryInfo info =
    //        DiaryInfo.builder()
    //            .challengedDate(diary.getCompletedDate().toString())
    //            .achievement(achievements)
    //            .achievementRate(achievementRate)
    //            .feeling(diary.getFeeling())
    //            .build();

    AuthorInfo author =
        AuthorInfo.builder()
            .id(member.getId())
            .nickname(member.getNickname())
            .profileImage(member.getProfileUrl())
            .build();

    //    Boolean isLiked =
    //        diary.getLikes().stream().anyMatch(like ->
    // like.getMember().getId().equals(member.getId()));
    //    int likeCount = diary.getLikes().size();

    //    List<String> imgUrl =
    //        diary.getImages().stream().map(DiaryImage::getUrl).collect(Collectors.toList());

    return DiaryResponse.builder()
        .id(diary.getId())
        .content(diary.getContent())
        .title(diary.getTitle())
        .isPublic(diary.getIsPublic())
        .authorInfo(author)
        // .diaryInfo(info)
        // .likedByMe(isLiked)
        // .likeCount(likeCount)
        // .imgUrl(imgUrl)
        .build();
  }
}
