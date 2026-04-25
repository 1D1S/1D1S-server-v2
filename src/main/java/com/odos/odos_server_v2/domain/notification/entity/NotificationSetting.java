package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_setting")
public class NotificationSetting extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_setting_id")
  private Long id;

  // 설정은 회원과 강한 연관관계로 가져가 라이프사이클을 함께 하거나, 아니면 결합도를 낮추기 위해 memberId로 가질 수 있음.
  // 보통 설정은 회원을 조회할 때 자주 같이 조회되거나 영속성 전이를 활용하기 위해 외래키 관계를 맺습니다.
  // 하지만 회원 조회 성능 저하를 방지하고 유지보수를 편하게 하기 위해 다른 도메인처럼 단방향 @OneToOne 매핑을 설정하거나 memberId를 사용합니다.
  // 여기서는 memberId로 결합도를 낮추어 구현합니다.
  @Column(name = "member_id", nullable = false, unique = true)
  private Long memberId;

  @Column(name = "friend_notification", nullable = false)
  private boolean friendNotification; // 친구 카테고리 알림 On/Off

  @Column(name = "diary_notification", nullable = false)
  private boolean diaryNotification; // 일지 카테고리 알림 On/Off

  @Column(name = "challenge_notification", nullable = false)
  private boolean challengeNotification; // 챌린지 카테고리 알림 On/Off

  @Builder
  public NotificationSetting(
      Long memberId,
      boolean friendNotification,
      boolean diaryNotification,
      boolean challengeNotification) {
    this.memberId = memberId;
    this.friendNotification = friendNotification;
    this.diaryNotification = diaryNotification;
    this.challengeNotification = challengeNotification;
  }

  public static NotificationSetting createDefault(Long memberId) {
    return NotificationSetting.builder()
        .memberId(memberId)
        .friendNotification(true)
        .diaryNotification(true)
        .challengeNotification(true)
        .build();
  }

  public void updateFriendNotification(boolean isEnabled) {
    this.friendNotification = isEnabled;
  }

  public void updateDiaryNotification(boolean isEnabled) {
    this.diaryNotification = isEnabled;
  }

  public void updateChallengeNotification(boolean isEnabled) {
    this.challengeNotification = isEnabled;
  }
}
