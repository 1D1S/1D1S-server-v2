# Notification Domain Design

This document details the design for the Notification domain's entities and repositories based on the requested features and architectural guidelines.

## 1. Entities

### 1.1 `Notification`
This is the core entity that represents a single notification sent to a user.

*   **Design:**
    *   `Long id`: Primary key.
    *   `Long receiverMemberId`: The ID of the member receiving the notification. Uses a wrapper `Long` instead of a `@ManyToOne` relationship to `Member` for loose coupling across domains.
    *   `Long actorMemberId`: The ID of the member who triggered the action (if applicable). Wrapper `Long` for loose coupling.
    *   `NotificationCategory category`: General grouping (FRIEND, DIARY, CHALLENGE).
    *   `NotificationType type`: Specific event type (e.g., FRIEND_REQUEST, MY_DIARY_COMMENTED).
    *   `NotificationTargetType targetType`: Where the user should land when clicking the notification (e.g., MEMBER_PROFILE, DIARY_DETAIL).
    *   `Long targetId`: The ID of the specific target (e.g., diaryId, challengeId). Wrapper `Long`.
    *   `Boolean isRead`: Indicates if the user has seen the notification. Defaults to `false`.
    *   `LocalDateTime readAt`: Timestamp of when it was read.
    *   `Integer groupedCount`: Useful for aggregated notifications like "[닉네임]님 외 N명이 댓글을 달았습니다". If null or 0, it's a single event.
    *   `LocalDateTime expiresAt`: Essential for the 30-day retention policy. Used by a scheduled task to clean up old entries.
    *   Inherits `createdAt` and `updatedAt` from `BaseTimeEntity`.

*   **Key Decisions & Reasons:**
    *   **No `message` field:** To comply with guidelines, pre-formatted strings are not stored. The message is dynamically generated at API response time based on `type`, `actorMemberId`, `groupedCount`, etc. This allows for easier changes to message copy or multi-language support in the future without updating database rows.
    *   **Loose Coupling:** Using `Long` for cross-domain IDs (`receiverMemberId`, `actorMemberId`, `targetId`) instead of strict JPA `@ManyToOne` connections. This improves module independence and prevents unnecessary eager fetching or complex join dependencies.
    *   **Data Retention:** The `expiresAt` field makes the 30-day cleanup policy efficient to query.

### 1.2 `NotificationPreference`
Manages user settings for which types of notifications they wish to receive.

*   **Design:**
    *   `Long id`: Primary key.
    *   `Long memberId`: The ID of the member holding these preferences. (Wrapper `Long`).
    *   `Boolean friendEnabled`: Toggle for friend-related notifications. Defaults to `true`.
    *   `Boolean diaryEnabled`: Toggle for diary-related notifications. Defaults to `true`.
    *   `Boolean challengeEnabled`: Toggle for challenge-related notifications. Defaults to `true`.
    *   Inherits `createdAt` and `updatedAt` from `BaseTimeEntity`.

*   **Key Decisions & Reasons:**
    *   Satisfies the "푸시 설정" requirement allowing categories (FRIEND, DIARY, CHALLENGE) to be turned On/Off independently.

### 1.3 `NotificationEvent` (Optional, if event sourcing is strictly needed)
*Note: Depending on the specific grouping strategy, creating a separate `NotificationEvent` entity to tie multiple `Notification` records to a single action might be overkill. Given the requirements, a `groupedCount` on the main `Notification` table usually suffices for simple aggregation. The existing code seems to have used it, but simplifying it to just the `Notification` entity is recommended unless there is a strong case for event separation.*

### 1.4 Enums Reorganization
*   **Design:** Move all enums from `com.odos.odos_server_v2.domain.notification.entity.Enum` to `com.odos.odos_server_v2.domain.notification.entity.enums`.
*   **Key Decisions & Reasons:** Complies with standard Java conventions where package names should be entirely lowercase, avoiding capitalized words like `Enum`.

## 2. Repositories

### 2.1 `NotificationRepository`
*   **Methods Needed:**
    *   `Page<Notification> findByReceiverMemberIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable)`: Fetch user's notifications.
    *   `long countByReceiverMemberIdAndIsReadFalse(Long receiverId)`: Get unread count.
    *   `void deleteByExpiresAtBefore(LocalDateTime now)`: For the scheduled 30-day cleanup task.
    *   `@Modifying @Query("...") int markAllAsRead(...)`: Bulk update.
    *   *Custom queries for grouping and 100-limit constraints.* For the maximum 100 entries per user policy, a separate process or query will be needed to identify the 100th oldest notification per user and delete those older than it, or simply enforcing it upon insertion.

### 2.2 `NotificationPreferenceRepository`
*   **Methods Needed:**
    *   `Optional<NotificationPreference> findByMemberId(Long memberId)`: Retrieve user settings before sending a push.

## 3. Policy Implementations to Note for Services

*   **Dynamic Message Generation:** A service or DTO layer component will take the `Notification` entity, fetch necessary domain data (like `actorMember.nickname`, `diary.title` - maybe via a facade or domain events to maintain loose coupling), and construct strings like `"[닉네임]님이 댓글을 달았습니다: [댓글 내용]"`.
*   **Like Milestones:** The service handling Like events needs stateful knowledge of the *total* likes on a diary to decide whether to trigger the milestone notification (1, 5, 10, 20... 1000+).
*   **Cleanup Schedule:** A `@Scheduled` method in a service will periodically call `notificationRepository.deleteByExpiresAtBefore(now)` to enforce the 30-day rule. The 100-entry rule will likely require a more complex query or a trigger-like mechanism during insertion to trim excess rows per user.
