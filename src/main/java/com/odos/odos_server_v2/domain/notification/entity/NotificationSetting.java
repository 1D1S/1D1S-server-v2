package com.odos.odos_server_v2.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification_setting")
public class NotificationSetting {

  // We use memberId as the primary key since it's a 1:1 relationship with Member
  @Id
  @Column(name = "member_id")
  private Long memberId;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isFriendNotificationEnabled = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isDiaryNotificationEnabled = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isChallengeNotificationEnabled = true;

  public void updateFriendNotification(Boolean enabled) {
    this.isFriendNotificationEnabled = enabled;
  }

  public void updateDiaryNotification(Boolean enabled) {
    this.isDiaryNotificationEnabled = enabled;
  }

  public void updateChallengeNotification(Boolean enabled) {
    this.isChallengeNotificationEnabled = enabled;
  }
}
