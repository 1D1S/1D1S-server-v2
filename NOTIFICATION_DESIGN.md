# Notification Domain Design

This document outlines the proposed design for the Notification domain's entities and repositories, adhering strictly to the provided requirements and architectural constraints.

## 1. Entities

All entities in this domain will extend `com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity` to automatically inherit auditing fields such as `createdAt` and `updatedAt`.

### 1.1. Notification

**Purpose:** Represents a single notification sent to a user.

**Design:**

```java
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "notification",
    indexes = {
      @Index(name = "idx_notification_receiver", columnList = "receiver_id"),
      @Index(name = "idx_notification_expires_at", columnList = "expires_at")
    })
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Loose Coupling: Using primitive ID instead of @ManyToOne(Member)
  @Column(name = "receiver_id", nullable = false)
  private Long receiverId;

  @Column(name = "actor_id")
  private Long actorId; // The user who triggered the notification

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationCategory category; // e.g., FRIEND, DIARY, CHALLENGE

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type; // e.g., FRIEND_REQUEST, MY_DIARY_COMMENTED

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private NotificationTargetType targetType; // Context type (e.g., DIARY, MEMBER, COMMENT)

  @Column(name = "target_id")
  private Long targetId; // Context ID (Loose Coupling)

  // Dynamic Message Generation: Instead of storing pre-formatted messages like "[닉네임]님이...",
  // we store a JSON payload containing only the necessary data (e.g., {"nickname": "User1", "diaryTitle": "Hello"})
  // The client or the response DTO mapper will format the actual message string based on the `type`.
  @Column(columnDefinition = "JSON")
  private String metadata;

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  // For notification grouping (e.g., "User1 외 N명이 댓글을 달았습니다.")
  @Column(name = "grouped_count")
  private Integer groupedCount;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt; // Used for 30-day data retention policy
}
```

**Reasoning:**
*   **Loose Coupling:** By using `Long receiverId`, `Long actorId`, and `Long targetId` instead of `@ManyToOne` relationships, we prevent tight coupling between the Notification domain and other domains (Member, Diary, Challenge). This makes the notification system more resilient and independent.
*   **Dynamic Messages:** Storing pre-formatted strings violates the memory constraint. By storing raw data in a `metadata` JSON field (or structured columns), the API layer can dynamically format the message (e.g., "UserA님이 친구 신청을 보냈어요.") based on the `NotificationType` at response time. This also helps with multi-language support in the future.
*   **Data Retention:** The `expiresAt` field allows scheduled jobs to easily query and delete old notifications (older than 30 days).

---

### 1.2. NotificationPreference

**Purpose:** Stores user-specific settings for receiving notifications.

**Design:**

```java
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

  // Loose Coupling
  @Column(name = "member_id", nullable = false, unique = true)
  private Long memberId;

  @Builder.Default
  @Column(name = "push_enabled", nullable = false)
  private Boolean pushEnabled = false;

  @Builder.Default
  @Column(name = "friend_enabled", nullable = false)
  private Boolean friendEnabled = false;

  @Builder.Default
  @Column(name = "diary_enabled", nullable = false)
  private Boolean diaryEnabled = false;

  @Builder.Default
  @Column(name = "challenge_enabled", nullable = false)
  private Boolean challengeEnabled = false;
}
```

**Reasoning:**
*   Satisfies the requirement: "유저 설정 내 `[알림 설정]` 페이지에서 카테고리별(친구/일지/챌린지) On/Off 기능 제공."
*   `memberId` is used instead of a direct Member reference to maintain domain isolation.

---

### 1.3. DiaryLikeMilestoneState

**Purpose:** Tracks the highest milestone reached for a specific diary's likes to avoid duplicate milestone notifications.

**Design:**

```java
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "diary_like_milestone_state")
public class DiaryLikeMilestoneState {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Loose Coupling
  @Column(name = "diary_id", nullable = false, unique = true)
  private Long diaryId;

  @Builder.Default
  @Column(name = "last_notified_milestone", nullable = false)
  private Integer lastNotifiedMilestone = 0;
}
```

**Reasoning:**
*   Satisfies the "좋아요 마일스톤" requirement (1, 5, 10, 20, 50, 100, 1000... likes).
*   By persisting the `lastNotifiedMilestone`, we ensure that a milestone notification is only sent once, even if the like count fluctuates around a milestone threshold.
*   Again, `diaryId` is used instead of `@OneToOne(Diary)`.

---

## 2. Repositories

### 2.1. NotificationRepository

```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  // Fetching paginated notifications for a user
  Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

  // Counting unread notifications
  long countByReceiverIdAndIsReadFalse(Long receiverId);

  // Data Retention Policies: 1. Delete older than 30 days
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
  void deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

  // Data Retention Policies: 2. Max 100 entries per user limit handling
  // This might involve native queries or custom implementation to keep only the top 100 per receiverId.
  // Alternatively, count total, and if > 100, find IDs to delete.

  @Modifying
  @Query("update Notification n set n.isRead = true, n.readAt = :readAt where n.receiverId = :receiverId and n.isRead = false")
  int markAllAsRead(@Param("receiverId") Long receiverId, @Param("readAt") LocalDateTime readAt);

  // Grouping lookup
  Optional<Notification> findFirstByReceiverIdAndTypeAndTargetIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
          Long receiverId, NotificationType type, Long targetId, LocalDateTime from);
}
```

**Reasoning:**
*   Includes queries to enforce the memory requirement: "최근 30일 혹은 최대 100건까지만 저장 후 자동 삭제." (Scheduled tasks will call these deletion methods).

### 2.2. NotificationPreferenceRepository

```java
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
  Optional<NotificationPreference> findByMemberId(Long memberId);
}
```

### 2.3. DiaryLikeMilestoneStateRepository

```java
public interface DiaryLikeMilestoneStateRepository extends JpaRepository<DiaryLikeMilestoneState, Long> {
  Optional<DiaryLikeMilestoneState> findByDiaryId(Long diaryId);
}
```
