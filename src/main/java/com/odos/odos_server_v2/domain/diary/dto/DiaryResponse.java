package com.odos.odos_server_v2.domain.diary.dto;

import lombok.Builder;

@Builder
public class DiaryResponse {
  private Long id;
  // private ChallengeResponse challenge;
  // private MemberResponse member;
  private String title;
  private String content;
  // private LikeInfo likeInfo;  -> likedByMe, count 속성
  // private List<String> imgUrl;
  private Boolean isPublic;
  private DiaryInfo diaryInfo;
}
