package com.odos.odos_server_v2.domain.notification.entity.Enum;

public enum NotificationType {
  // 친구 관련
  FRIEND_REQUEST,
  FRIEND_ACCEPT,

  // 일지 및 커뮤니티
  FRIEND_DIARY_CREATED,
  DIARY_COMMENT_CREATED,
  DIARY_COMMENT_REPLY_CREATED,
  DIARY_LIKE_MILESTONE, // 좋아요 마일스톤 달성

  // 챌린지 관련
  CHALLENGE_ACCEPTED,
  CHALLENGE_REJECTED
}
