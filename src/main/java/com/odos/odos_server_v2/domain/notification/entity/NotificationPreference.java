package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification_preference")
public class NotificationPreference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private Member member;

  @Builder.Default
  @Column(name = "push_enabled", nullable = false)
  private Boolean pushEnabled = true;

  @Builder.Default
  @Column(name = "friend_enabled", nullable = false)
  private Boolean friendEnabled = true;

  @Builder.Default
  @Column(name = "diary_enabled", nullable = false)
  private Boolean diaryEnabled = true;

  @Builder.Default
  @Column(name = "challenge_enabled", nullable = false)
  private Boolean challengeEnabled = true;

  public void update(
      Boolean pushEnabled, Boolean friendEnabled, Boolean diaryEnabled, Boolean challengeEnabled) {
    this.pushEnabled = pushEnabled;
    this.friendEnabled = friendEnabled;
    this.diaryEnabled = diaryEnabled;
    this.challengeEnabled = challengeEnabled;
  }
}
