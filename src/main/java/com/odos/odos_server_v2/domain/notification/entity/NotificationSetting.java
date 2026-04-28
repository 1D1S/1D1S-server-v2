package com.odos.odos_server_v2.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification_setting")
public class NotificationSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_setting_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private Long memberId;

  @Builder.Default
  @Column(nullable = false)
  private Boolean friendEnabled = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean diaryEnabled = true;

  @Builder.Default
  @Column(nullable = false)
  private Boolean challengeEnabled = true;

  public void updateSettings(
      Boolean friendEnabled, Boolean diaryEnabled, Boolean challengeEnabled) {
    if (friendEnabled != null) {
      this.friendEnabled = friendEnabled;
    }
    if (diaryEnabled != null) {
      this.diaryEnabled = diaryEnabled;
    }
    if (challengeEnabled != null) {
      this.challengeEnabled = challengeEnabled;
    }
  }
}
