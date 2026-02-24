package com.odos.odos_server_v2.domain.diary.dto;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryGoal;
import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.dto.LikeDto;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DiaryResponse {

  private Long id;
  private ChallengeSummaryResponse challenge;
  private AuthorInfoDto author;
  private String title;
  private String content;
  private List<String> imgUrl;
  private Boolean isPublic;
  private LikeDto likeInfo;
  private DiaryInfoDto diaryInfo;

  public static DiaryResponse from(Member member, Diary diary, ChallengeSummaryResponse challenge) {

    List<Long> achievements =
        diary.getDiaryGoals() == null
            ? Collections.emptyList()
            : diary.getDiaryGoals().stream()
                .filter(DiaryGoal::getIsCompleted)
                .map(diaryGoal -> diaryGoal.getChallengeGoal().getId()) // 챌린지골 아이디 기준으로
                .toList();

    int totalGoal = diary.getDiaryGoals().size();
    long achievedGoalsCount =
        diary.getDiaryGoals().stream().filter(DiaryGoal::getIsCompleted).count();
    int achievementRate = totalGoal > 0 ? (int) (achievedGoalsCount * 100) / totalGoal : 0;

    DiaryInfoDto info =
        DiaryInfoDto.builder()
            .createdAt(diary.getCreatedDate().toString())
            .challengedDate(diary.getCompletedDate().toString())
            .achievement(achievements)
            .achievementRate(achievementRate)
            .feeling(diary.getFeeling())
            .build();

    AuthorInfoDto author =
        AuthorInfoDto.builder()
            .id(diary.getMember().getId())
            .nickname(diary.getMember().getNickname())
            .profileImage(diary.getMember().getProfileUrl())
            .build();

    List<DiaryLike> likes = diary.getLikes() == null ? List.of() : diary.getLikes();

    long likeCount = likes.size();

    boolean likedByMe =
        likes.stream()
            .anyMatch(
                like ->
                    like.getMember() != null
                        && like.getMember().getId() != null
                        && member != null
                        && member.getId() != null
                        && like.getMember().getId().equals(member.getId()));

    LikeDto like =
        LikeDto.builder()
            .likedByMe(likedByMe) // 임의 넣기, 나중에 수정해야햄
            .likeCnt(likeCount)
            .build();

    //    List<String> imgUrl =
    //        diary.getImages().stream().map(DiaryImage::getUrl).collect(Collectors.toList());

    return DiaryResponse.builder()
        .id(diary.getId())
        .challenge(challenge)
        .content(diary.getContent())
        .title(diary.getTitle())
        .isPublic(diary.getIsPublic())
        .author(author)
        .diaryInfo(info)
        .likeInfo(like)
        // .imgUrl(imgUrl)
        .build();
  }
}
