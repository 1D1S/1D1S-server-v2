# 알림(Notification) 기능 엔티티 및 레포지토리 설계안

본 문서는 요구사항에 명시된 알림(Notification) 도메인의 엔티티 및 레포지토리 설계와 그에 대한 이유를 설명합니다.

## 1. 엔티티 (Entity) 설계

### 1.1 `Notification` 엔티티

알림의 기본 정보를 담는 핵심 엔티티입니다.

```java
package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "notification",
    indexes = {
      @Index(name = "idx_notification_receiver_created_at", columnList = "receiver_member_id, created_at"),
      @Index(name = "idx_notification_expires_at", columnList = "expires_at"),
      @Index(name = "idx_notification_receiver_is_read", columnList = "receiver_member_id, is_read")
    })
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_member_id", nullable = false)
  private Member receiver; // 알림 수신자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_member_id")
  private Member actor; // 알림 발생자 (예: 댓글 작성자, 친구 신청자)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationCategory category; // FRIEND, DIARY, CHALLENGE

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type; // 구체적인 알림 타입 (예: FRIEND_REQUEST, MY_DIARY_COMMENTED 등)

  // 동적 메시지 생성을 위해 DB에 message(예: "[닉네임]님이 댓글을 달았습니다")를 직접 저장하지 않습니다.
  // API 응답 시 actor의 닉네임, 알림 타입, targetId 등을 활용하여 조합합니다.

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private NotificationTargetType targetType; // 랜딩 대상 (MEMBER_PROFILE, DIARY_DETAIL, CHALLENGE_DETAIL 등)

  @Column(name = "target_id")
  private Long targetId; // 랜딩 페이지 이동에 필요한 ID. 도메인 간 강결합 방지를 위해 @ManyToOne 대신 Long 타입 사용.

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false; // 읽음 여부

  @Column(name = "read_at")
  private LocalDateTime readAt; // 읽은 시간

  @Column(name = "grouped_count")
  private Integer groupedCount; // 알림 그룹화를 위한 카운트 (예: "[닉네임]님 외 N명")

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt; // 알림 만료 시간 (보존 기한 처리용)

  public void markAsRead() {
    this.isRead = true;
    this.readAt = LocalDateTime.now();
  }
}
```

**설계 이유:**
* **`message` 필드 제거:** 기존에 문자열로 저장되던 `message` 필드를 제거했습니다. 이는 알림 내용이 유저의 닉네임 변경 등에 동적으로 대응할 수 있도록 API 반환 시점에 조합(Generate dynamically)하기 위함입니다.
* **느슨한 결합 (Loose Coupling):** `Diary`, `Challenge` 엔티티를 직접 `@ManyToOne`으로 참조하지 않고, `targetId` (`Long` 타입)와 `targetType`을 사용하여 결합도를 낮췄습니다.
* **인덱싱:** 조회 성능 향상을 위해 수신자와 생성일시, 수신자와 읽음여부, 그리고 보존 기한 관리를 위한 만료일시(`expires_at`)에 인덱스를 추가했습니다.
* **`expiresAt` 필드:** 30일 보존 정책을 쉽게 구현하기 위해 만료 시간을 명시적으로 저장합니다.

### 1.2 `NotificationPreference` 엔티티

유저의 알림 수신 설정(On/Off)을 관리하는 엔티티입니다.

```java
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

  public void update(Boolean pushEnabled, Boolean friendEnabled, Boolean diaryEnabled, Boolean challengeEnabled) {
    this.pushEnabled = pushEnabled;
    this.friendEnabled = friendEnabled;
    this.diaryEnabled = diaryEnabled;
    this.challengeEnabled = challengeEnabled;
  }
}
```

**설계 이유:**
* **카테고리별 분리:** 요구사항에 맞게 전체 푸시, 친구 관련, 일지 관련, 챌린지 관련 알림 On/Off를 독립적인 boolean 필드로 관리합니다.

### 1.3 `NotificationEvent` 엔티티

알림 이벤트를 추적하거나 그룹화된 메시지를 구성하는 데 사용됩니다.

```java
package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification_event")
public class NotificationEvent extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_member_id")
  private Member actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private NotificationTargetType targetType;

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "grouped_count")
  private Integer groupedCount; // 알림 그룹화를 위한 카운트 (추후 로직 처리에 활용)
}
```
**설계 이유:**
* 동일하게 `message` 필드를 제거하고, 다수의 알림을 하나로 그룹화할 때 베이스가 되는 이벤트의 정보를 기록합니다.

---

## 2. 레포지토리 (Repository) 설계

데이터 접근 및 30일/최대 100건 보존 정책 처리를 위한 레포지토리 메서드입니다.

```java
package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.notification.entity.Enum.NotificationType;
import com.odos.odos_server_v2.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // 수신자의 알림 목록 최신순 조회
  Page<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable);

  // 안 읽은 알림 개수 조회
  long countByReceiverAndIsReadFalse(Member receiver);

  // 전체 알림 개수 조회 (100건 제한 체크용)
  long countByReceiver(Member receiver);

  // 1. 만료 시간(expires_at)이 지난 알림 삭제 (30일 보존 기한 정책)
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
  void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

  // 2. 최대 100건 보존 정책을 위한 쿼리 (가장 오래된 알림들 삭제)
  // JPA는 DELETE 쿼리에서 LIMIT이나 서브쿼리 사용이 제한적이므로,
  // 주로 Service 레이어에서 삭제 대상 ID 목록을 구한 후 아래 메서드를 호출하여 삭제합니다.
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.id IN :ids")
  void deleteByIdIn(@Param("ids") java.util.List<Long> ids);

  // 수신자 기준 가장 오래된 알림 조회 (100건 초과 시 삭제 대상을 찾기 위함)
  @Query("SELECT n.id FROM Notification n WHERE n.receiver = :receiver ORDER BY n.createdAt ASC")
  java.util.List<Long> findOldestNotificationIdsByReceiver(@Param("receiver") Member receiver, Pageable pageable);

  // 수신자의 모든 알림을 읽음 처리
  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.receiver = :receiver AND n.isRead = false")
  int markAllAsRead(@Param("receiver") Member receiver, @Param("readAt") LocalDateTime readAt);

  // 알림 그룹화 처리를 위해 기존 알림 조회 (예: 동일 게시글의 최근 댓글 알림)
  Optional<Notification> findFirstByReceiverAndTypeAndTargetIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
          Member receiver, NotificationType type, Long targetId, LocalDateTime from);
}
```

**설계 이유:**
* **보존 기한 처리 (30일):** `deleteByExpiresAtBefore` 메서드를 통해 `@Scheduled` 배치 작업에서 매일/매시간 만료된 알림을 쉽게 일괄 삭제할 수 있도록 구성했습니다.
* **보존 한도 처리 (최대 100건):** 특정 수신자의 알림이 100건을 넘었을 때 삭제를 수행하기 위해 ID 목록으로 삭제하는 `deleteByIdIn` 과 삭제 대상을 조회하는 `findOldestNotificationIdsByReceiver`를 정의했습니다.
* **그룹화 로직 지원:** 동일 게시물(targetId)에 짧은 시간 내(from) 댓글이 달렸을 때 기존 알림을 찾아서 카운트(`groupedCount`)를 올리기 위한 메서드를 구성했습니다.
