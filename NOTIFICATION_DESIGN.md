# 알림(Notification) 기능 엔티티 및 레포지토리 설계안

요구사항을 바탕으로 알림 기능의 DB 엔티티와 레포지토리를 설계했습니다. 현재 코드베이스의 구조와 도메인 주도 설계(DDD) 및 메모리 지침(Auditing, Enum 네이밍 규칙, 동적 메시지 생성 등)을 고려하여 작성되었습니다.

## 1. Entity 설계안

### 1.1 Notification (알림 엔티티)
알림의 기본 정보를 담는 핵심 엔티티입니다.

```java
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
    }
)
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_member_id", nullable = false)
  private Member receiver; // 알림 수신자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_member_id")
  private Member actor; // 알림을 발생시킨 사용자 (친구 신청자, 댓글 작성자 등)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationCategory category; // 알림 대분류 (FRIEND, DIARY, CHALLENGE)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private NotificationType type; // 구체적인 알림 타입 (FRIEND_REQUEST, MY_DIARY_COMMENTED 등)

  // 메모리 지침에 따라 알림 메시지를 직접 저장하지 않고, 동적 생성에 필요한 인자(Args)를 JSON 또는 단순 문자열로 저장
  @Column(name = "content_args", nullable = false, columnDefinition = "TEXT")
  private String contentArgs;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private NotificationTargetType targetType; // 랜딩할 타겟 화면 유형

  @Column(name = "target_id")
  private Long targetId; // 랜딩할 타겟의 ID (예: 일지 ID, 챌린지 ID). 느슨한 결합을 위해 객체 연관관계 대신 ID만 저장

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false; // 읽음 여부

  @Column(name = "read_at")
  private LocalDateTime readAt; // 읽은 시간

  @Column(name = "grouped_count")
  private Integer groupedCount; // 알림 그룹화를 위한 카운트 (예: "외 N명")

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt; // 보존 기한 관리를 위한 만료 일시 (기본 30일 설정 등)
}
```

**설계 이유:**
* **`contentArgs` 사용:** "Do not store pre-formatted dynamic message strings in the database; generate them dynamically at API response time based on entity data"라는 메모리 지침에 따라 고정된 `message` 필드 대신 메시지 조립에 필요한 인자를 저장하도록 설계했습니다.
* **느슨한 결합 (`targetId`):** 다른 도메인(일지, 챌린지 등)과 하드하게 `@ManyToOne`으로 엮지 않고 `targetId` 필드를 두어 JPA 연관관계를 분리했습니다.
* **인덱스 설정:** 알림 센터 조회 시 수신자 기준 최신순 정렬(`receiver_member_id`, `created_at`), 만료일 스케줄러 삭제(`expires_at`), 안 읽은 알림 배지 조회(`receiver_member_id`, `is_read`) 쿼리 성능을 위해 복합 인덱스를 적용했습니다.

### 1.2 NotificationPreference (알림 설정 엔티티)
유저별 카테고리 알림 On/Off를 관리합니다.

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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private Member member;

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

**설계 이유:**
* 유저 설정 내 `[알림 설정]` 페이지에서 카테고리별(친구/일지/챌린지) On/Off 기능을 제공해야 한다는 정책(3.1)을 반영했습니다.

### 1.3 관련 Enum 클래스들 (패키지: `com.odos.odos_server_v2.domain.notification.entity.enums`)
*메모리 지침에 따라 Enum 패키지명은 소문자 `enums`로 사용합니다.*

```java
public enum NotificationCategory {
  FRIEND, DIARY, CHALLENGE
}

public enum NotificationType {
  FRIEND_REQUEST,
  FRIEND_ACCEPT,
  FRIEND_DIARY_CREATED,
  MY_DIARY_COMMENTED,
  MY_COMMENT_REPLIED,
  DIARY_LIKE_MILESTONE,
  CHALLENGE_APPLIED,
  CHALLENGE_APPROVED,
  CHALLENGE_REJECTED
}

public enum NotificationTargetType {
  MEMBER_PROFILE,
  DIARY_DETAIL,
  DIARY_COMMENT,
  CHALLENGE_DETAIL,
  CHALLENGE_LIST,
  MAIN
}
```

**설계 이유:**
* 기획서에 명시된 알림 유형별로 메시지를 동적 포맷팅하고 랜딩 페이지를 분기 처리할 수 있도록 명확히 분류했습니다.

---

## 2. Repository 설계안

### 2.1 NotificationRepository

```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // 1. 알림 센터 목록 조회 (최신순 페이징)
  Page<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable);

  // 2. 안 읽은 알림 배지(개수) 조회
  long countByReceiverAndIsReadFalse(Member receiver);

  // 3. 만료된 알림 삭제 (스케줄러 동작용, 30일 경과)
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
  void deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

  // 4. 알림 일괄 읽음 처리 (알림 센터 진입 시)
  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.receiver = :receiver AND n.isRead = false")
  int markAllAsRead(@Param("receiver") Member receiver, @Param("readAt") LocalDateTime readAt);

  // 5. 짧은 시간 내 알림 그룹화를 위한 동일 타겟 대상 최신 알림 조회
  Optional<Notification> findFirstByReceiverAndTypeAndTargetIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
      Member receiver, NotificationType type, Long targetId, LocalDateTime from);
}
```

**설계 이유:**
* UI 노출에 필요한 페이징(`findByReceiverOrderByCreatedAtDesc`) 및 읽지 않음 카운트(`countByReceiverAndIsReadFalse`) 메서드를 포함했습니다.
* **데이터 보존 정책 (3.2)**: 30일 보존 기한을 맞추기 위해 Spring `@Scheduled`로 주기적으로 호출할 `deleteByExpiresAtBefore` 벌크 연산을 설계했습니다.
* **알림 그룹화 정책 (3.1)**: "A님 외 N명이 댓글을 남겼습니다" 구현을 위해, 일정 시간 내 동일 게시물(targetId)에 달린 최근 알림을 찾아 `groupedCount`를 증가시키는 방식으로 구현할 수 있도록 `findFirstBy...` 쿼리 메서드를 정의했습니다.

### 2.2 NotificationPreferenceRepository

```java
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
  Optional<NotificationPreference> findByMember(Member member);
}
```
**설계 이유:** 알림 발송 전(Dispatch 단계) 수신자의 카테고리 수신 동의 여부를 확인하기 위해 조회용 메서드만 기본 제공합니다.
