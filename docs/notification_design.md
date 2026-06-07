# Notification Domain Design Document

## 1. Entity Design (`Notification`)

### 1.1 Source Code

```java
package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.notification.enums.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받는 사용자 (수신자)
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    // 알림을 발생시킨 사용자 (발신자) - 시스템 알림일 경우 null 가능
    @Column(name = "sender_id")
    private Long senderId;

    // 알림 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // 알림과 연관된 도메인 엔티티의 ID (예: 일지 ID, 댓글 ID, 챌린지 ID 등)
    @Column(name = "target_id")
    private Long targetId;

    // 부가 데이터 (좋아요 마일스톤 달성 개수 등)
    @Column(name = "meta_data")
    private String metaData;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
    }
}
```

### 1.2 Design Reasons
- **Loose Coupling:** Using `Long receiverId`, `Long senderId`, and `Long targetId` instead of `@ManyToOne` mapping avoids strong dependencies between `Notification` and other domain entities like `Member`, `Diary`, `Challenge`, etc. This aligns with standard microservices/modular monolithic architectural practices.
- **BaseTimeEntity Inheritance:** Extending `BaseTimeEntity` automatically handles the `createdAt` field for checking the 30-day retention policy.
- **No Pre-formatted Messages:** Following the requirement not to store string messages directly, this entity relies on `notificationType`, `senderId`, `targetId`, and `metaData` to dynamically construct notifications at API response time.
- **@Builder.Default for isRead:** Ensures the `isRead` flag is properly initialized to `false` when using the builder pattern, avoiding compilation warnings.

---

## 2. Enum Design (`NotificationType`)

### 2.1 Source Code

```java
package com.odos.odos_server_v2.domain.notification.enums;

public enum NotificationType {
    // 친구 관련
    FRIEND_REQUEST,
    FRIEND_ACCEPT,

    // 일지 및 커뮤니티
    FRIEND_DIARY_POST,
    DIARY_COMMENT,
    COMMENT_REPLY,

    // 좋아요 마일스톤 (누적 알림)
    LIKE_MILESTONE,

    // 챌린지 관련
    CHALLENGE_ACCEPT,
    CHALLENGE_REJECT;
}
```

### 2.2 Design Reasons
- **Package Naming:** Defined under `enums` package instead of `Enum` to adhere to Java lower-case package naming conventions and avoid reserved keywords.
- **Categorization:** Covered all the event types described in the requirement (Friend, Diary/Community, Like Milestone, Challenge).

---

## 3. Repository Design (`NotificationRepository`)

### 3.1 Source Code

```java
package com.odos.odos_server_v2.domain.notification.repository;

import com.odos.odos_server_v2.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. 유저별 알림 목록 조회 (최신순)
    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // 2. 알림 삭제 - 30일이 지난 알림 (스케줄러용)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :thresholdDate")
    int deleteOldNotifications(@Param("thresholdDate") LocalDateTime thresholdDate);

    // 3. 알림 삭제 - 특정 유저의 알림 갯수가 100개를 초과할 때 가장 오래된 알림들 삭제
    // (JPA에서 LIMIT/OFFSET 처리가 까다로울 수 있으므로 ID 기준으로 삭제하는 방법 사용)
    @Modifying
    @Query(value = "DELETE FROM notification WHERE id IN (" +
            "SELECT id FROM (" +
            "SELECT id FROM notification WHERE receiver_id = :receiverId " +
            "ORDER BY created_at DESC LIMIT 10000 OFFSET 100" +
            ") as tmp)", nativeQuery = true)
    int deleteExceedingNotifications(@Param("receiverId") Long receiverId);

    // 4. 유저별 알림 전체 삭제 (회원 탈퇴 등)
    void deleteAllByReceiverId(Long receiverId);
}
```

### 3.2 Design Reasons
- **Data Retention Policy Support:** Custom queries are prepared to satisfy the automatic cleanup policy (deleting entries older than 30 days or exceeding 100 entries per user).
- **Native Query for Limit/Offset Deletion:** Because JPQL does not fully support `LIMIT` in subqueries for bulk deletion, a native query approach is used to remove items beyond the 100-item limit per user.