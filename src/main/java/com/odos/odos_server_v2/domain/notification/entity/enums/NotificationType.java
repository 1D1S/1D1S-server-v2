package com.odos.odos_server_v2.domain.notification.entity.enums;

public enum NotificationType {
  // 친구 관련
  FRIEND_REQUEST, // 친구 신청
  FRIEND_ACCEPT, // 친구 수락

  // 일지 및 커뮤니티
  FRIEND_DIARY_CREATED, // 친구 일지 등록
  DIARY_COMMENT, // 내 일지 댓글
  COMMENT_REPLY, // 내 댓글 대댓글

  // 좋아요 마일스톤
  DIARY_LIKE_MILESTONE, // 좋아요 마일스톤 (1, 5, 10, 20, 50, 100, 1000... 달성)

  // 챌린지 관련
  CHALLENGE_ACCEPTED, // 챌린지 승낙
  CHALLENGE_REJECTED // 챌린지 거절
}
