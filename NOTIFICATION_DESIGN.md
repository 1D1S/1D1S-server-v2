# Notification Domain Design

## 1. Package Structure Adjustment
**Current:** `com.odos.odos_server_v2.domain.notification.entity.Enum`
**New:** `com.odos.odos_server_v2.domain.notification.entity.enums`
**Reason:** Package names for enums should be completely lowercase, avoiding capitalized words like `Enum` as a package name to comply with Java conventions.

## 2. Entity Design

### A. Notification Entity

```java
package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationCategory;
import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationTargetType;
import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "notification",
    indexes = {
        @Index(name = "idx_notification_receiver_created_at", columnList = "receiver_id, created_at"),
        @Index(name = "idx_notification_expires_at", columnList = "expires_at"),
        @Index(name = "idx_notification_receiver_is_read", columnList = "receiver_id, is_read")
    }
)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reason: Prefer loose coupling using primitive/wrapper ID fields instead of strict JPA object relationships
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "actor_id")
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    // The 'message' field is intentionally omitted.
    // Reason: Do not store pre-formatted dynamic message strings in the database;
    // generate them dynamically at API response time based on entity data.

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // Reason: Used @Builder.Default to prevent compilation warnings.

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "grouped_count")
    private Integer groupedCount;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Extends BaseTimeEntity to inherit standard auditing fields like createdAt
}
```

#### Design Justification
1. **Loose Coupling (`receiverId`, `actorId`):** Replaced `@ManyToOne Member` with `Long receiverId` and `Long actorId`. This adheres to the guideline of preferring loose coupling using primitive ID fields for cross-domain referencing.
2. **Dynamic Messaging:** Removed the `message` field. Pre-formatted message strings should not be stored. The actual notification text (e.g., `[닉네임]님이 친구 신청을 보냈어요.`) will be generated dynamically on the client side or at API response time based on `type`, `actorId`, and `groupedCount`.
3. **Auditing:** Extends `BaseTimeEntity` to automatically handle `createdAt` and `updatedAt` for data retention and sorting purposes.
4. **Data Retention Support:** `expiresAt` field allows for easy implementation of the "delete older than 30 days" policy.

### B. NotificationPreference Entity

```java
package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification_preference")
public class NotificationPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reason: Prefer loose coupling using primitive/wrapper ID fields
    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Builder.Default
    @Column(name = "friend_alert_enabled", nullable = false)
    private Boolean friendAlertEnabled = true;

    @Builder.Default
    @Column(name = "diary_alert_enabled", nullable = false)
    private Boolean diaryAlertEnabled = true;

    @Builder.Default
    @Column(name = "challenge_alert_enabled", nullable = false)
    private Boolean challengeAlertEnabled = true;
}
```

#### Design Justification
1. **Category On/Off:** Provides boolean flags for friend, diary, and challenge notifications as requested in the requirements (`카테고리별(친구/일지/챌린지) On/Off 기능 제공`).
2. **Loose Coupling:** Uses `Long memberId` instead of mapping directly to the `Member` entity.

---

## 3. Repository Design

### A. NotificationRepository

```java
package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Fetch user's notifications sorted by latest
    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    // Data Retention: Delete older than 30 days
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Data Retention: Delete excess notifications keeping only top 100
    @Modifying
    @Query(value = "DELETE FROM notification WHERE receiver_id = :receiverId AND id NOT IN " +
                   "(SELECT id FROM (SELECT id FROM notification WHERE receiver_id = :receiverId " +
                   "ORDER BY created_at DESC LIMIT 100) as t)", nativeQuery = true)
    void deleteExcessNotifications(@Param("receiverId") Long receiverId);
}
```

#### Design Justification
1. **Data Retention Policies:** Includes queries to delete records older than 30 days and to enforce the maximum limit of 100 entries per user, fulfilling the requirement (`최근 30일 혹은 최대 100건까지만 저장 후 자동 삭제`). These methods should be called by a `@Scheduled` background task.
2. **Fetch by Receiver:** Uses `receiverId` to efficiently load notifications for a user, relying on the established composite index `idx_notification_receiver_created_at`.

### B. NotificationPreferenceRepository

```java
package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByMemberId(Long memberId);
}
```

#### Design Justification
1. Allows quick retrieval of a user's notification settings using their `memberId` to decide whether a push notification should be sent or suppressed based on the category.
