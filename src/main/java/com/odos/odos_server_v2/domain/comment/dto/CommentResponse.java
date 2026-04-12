package com.odos.odos_server_v2.domain.comment.dto;

import com.odos.odos_server_v2.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {

  private Long commentId;
  private String content;
  private Long authorId;
  private String authorNickname;
  private String authorProfileUrl;
  private String createdAt;
  private long replyCount;
  private boolean isDeleted;

  public static CommentResponse from(Comment comment, String authorProfileUrl, long replyCount) {

    return CommentResponse.builder()
        .commentId(comment.getId())
        .content(comment.getIsDeleted() ? null : comment.getContent())
        .authorId(comment.getIsDeleted() ? null : comment.getMember().getId())
        .authorNickname(comment.getIsDeleted() ? null : comment.getMember().getNickname())
        .authorProfileUrl(comment.getIsDeleted() ? null : authorProfileUrl)
        .createdAt(comment.getCreatedAt().toString())
        .replyCount(replyCount)
        .isDeleted(comment.getIsDeleted())
        .build();
  }
}
