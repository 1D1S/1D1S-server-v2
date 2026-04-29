package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long receiverId;

  @Column private Long senderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  // e.g., diaryId, challengeId, commentId
  @Column private Long targetId;

  // Extensible field for dynamic message generation (e.g., number of likes, friend's name if needed
  // beyond senderId)
  @Column private String parameter;

  @Column(nullable = false)
  private boolean isRead = false;

  @Builder
  public Notification(
      Long receiverId, Long senderId, NotificationType type, Long targetId, String parameter) {
    this.receiverId = receiverId;
    this.senderId = senderId;
    this.type = type;
    this.targetId = targetId;
    this.parameter = parameter;
    this.isRead = false;
  }

  public void markAsRead() {
    this.isRead = true;
  }
}
