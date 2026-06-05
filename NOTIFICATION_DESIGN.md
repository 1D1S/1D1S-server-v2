# 알림(Notification) 도메인 설계 문서

## 1. 개요
본 문서는 사용자 요구사항과 시스템 가이드라인을 바탕으로 알림(Notification) 도메인의 엔티티(Entity) 및 레포지토리(Repository) 설계를 정의합니다.

### 설계 핵심 원칙
1. **동적 메시지 생성:** 알림 메시지는 DB에 하드코딩된 문자열로 저장하지 않고, API 응답 시 엔티티 데이터를 기반으로 동적으로 생성합니다.
2. **느슨한 결합(Loose Coupling):** 다른 도메인(Member, Diary, Challenge 등)과의 연관관계는 `@ManyToOne`과 같은 강한 결합 대신 `Long targetId`, `Long actorId` 등 기본형/래퍼 클래스 ID 필드를 사용하여 시스템 간 결합도를 낮춥니다.
3. **데이터 보존 정책:** 알림은 최대 30일 보관 및 유저당 최대 100건까지만 유지하도록 설계하며, 이를 위한 스케줄링 처리용 필드를 포함합니다.
4. **패키지 네이밍 컨벤션:** Enum 클래스는 자바 네이밍 규칙에 맞게 소문자 패키지명(`enums`) 하위에 구성합니다.

---

## 2. 엔티티(Entity) 설계

모든 엔티티는 공통 감사 필드를 위해 `BaseTimeEntity`를 상속(`extends BaseTimeEntity`)받습니다.

### 2.1 Notification (알림 본체)
알림 내역을 저장하는 핵심 엔티티입니다.

| 필드명 | 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | `Long` | PK, Auto Increment | 알림 고유 ID |
| `receiverId` | `Long` | Not Null, Index | 알림을 받는 유저의 ID (Loose Coupling) |
| `actorId` | `Long` | Nullable | 알림 발생을 유발한 상대방 유저의 ID |
| `category` | `NotificationCategory` (Enum) | Not Null | 알림 대분류 (FRIEND, DIARY, CHALLENGE) |
| `type` | `NotificationType` (Enum) | Not Null | 알림 상세 타입 (FRIEND_REQUEST, DIARY_LIKE_MILESTONE 등) |
| `targetId` | `Long` | Nullable | 클릭 시 랜딩될 대상의 ID (diaryId, challengeId 등) |
| `relatedData` | `String` | Nullable, Text | 일지 제목, 챌린지 이름 등 메시지 동적 생성에 필요한 메타데이터 (JSON 형태로 저장 권장) |
| `isRead` | `Boolean` | Not Null, Default=false | 알림 읽음 여부 (`@Builder.Default` 사용) |
| `groupedCount` | `Integer` | Default=0 | 동일 게시물 댓글 등 그룹화된 알림 발생 시 누적 횟수 |
| `expiresAt` | `LocalDateTime` | Not Null, Index | 알림 만료 시간 (생성일로부터 30일 뒤) |

**설계 이유 (Justification):**
- **메시지 필드 제거:** 기존에 저장되던 미리 포맷팅된 `message` 필드를 제거하고, API 서빙 시점에 `actorId`, `targetId`, `relatedData`를 조합하여 메시지를 동적으로 구성합니다. 이렇게 하면 추후 알림 메시지 포맷이 변경되더라도 기존 데이터에 소급 적용이 용이합니다.
- **Loose Coupling:** `receiver`, `actor` 필드를 `@ManyToOne Member` 대신 `Long` 타입 ID로 변경하여 다른 도메인에 대한 JPA의 강력한 의존성을 제거했습니다. `targetId` 또한 여러 도메인의 식별자를 담을 수 있어 확장성이 뛰어납니다.
- **관련 데이터(relatedData):** 일지나 챌린지가 삭제되었을 때에도 알림 메시지에서 '삭제된 일지 제목'을 보여줄 수 있도록 최소한의 스냅샷 데이터를 저장합니다.
- **expiresAt & Index:** 최근 30일 보존 기한 정책을 만족하고 `@Scheduled` 스케줄러 삭제 작업의 쿼리 성능을 높이기 위해 인덱스를 부여합니다.

### 2.2 NotificationPreference (알림 수신 설정)
유저의 카테고리별 푸시 알림 수신 설정(On/Off)을 관리합니다.

| 필드명 | 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | `Long` | PK, Auto Increment | 설정 고유 ID |
| `memberId` | `Long` | Not Null, Unique | 대상 유저 ID (Loose Coupling) |
| `isFriendPushEnabled` | `Boolean` | Not Null, Default=true | 친구 관련 알림 수신 여부 |
| `isDiaryPushEnabled` | `Boolean` | Not Null, Default=true | 일지 관련 알림 수신 여부 |
| `isChallengePushEnabled`| `Boolean` | Not Null, Default=true | 챌린지 관련 알림 수신 여부 |

**설계 이유 (Justification):**
- **기능 명세 반영:** 요구사항 3.1 항목에 따라 유저가 알림 카테고리(친구/일지/챌린지)별로 On/Off를 설정할 수 있는 기능을 제공하기 위함입니다. `memberId`를 기반으로 조회하여 푸시 발송 시 검사 로직에 사용합니다.

### 2.3 DiaryLikeMilestoneState (좋아요 누적 알림 상태 관리)
게시물별 좋아요 수 누적 돌파(1, 5, 10, 20...) 현황을 추적합니다.

| 필드명 | 타입 | 제약 조건 | 설명 |
|---|---|---|---|
| `id` | `Long` | PK, Auto Increment | 고유 ID |
| `diaryId` | `Long` | Not Null, Unique | 상태를 추적할 대상 일지 ID |
| `lastNotifiedLikeCount` | `Integer` | Not Null, Default=0 | 가장 마지막으로 발송된 마일스톤의 기준 좋아요 수 |

**설계 이유 (Justification):**
- **마일스톤 정책 지원:** 1, 5, 10, 20, 50, 100, 1000 단위로 축하 알림을 발송하려면 '어디까지 마일스톤 알림이 나갔는지'에 대한 상태가 필수적입니다. 매번 좋아요 개수를 다시 카운팅하거나 알림 테이블을 뒤지는 것보다 단일 엔티티로 상태를 관리하는 것이 성능 및 관리에 효율적입니다.

---

## 3. 레포지토리(Repository) 설계

### 3.1 NotificationRepository
```java
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 1. 특정 유저의 알림 목록 조회 (최신순 등)
    Page<Notification> findByReceiverId(Long receiverId, Pageable pageable);

    // 2. 안 읽은 알림 개수 조회
    int countByReceiverIdAndIsReadFalse(Long receiverId);

    // 3. 30일 경과 알림 자동 삭제 처리 (Batch Scheduler 연동)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
    void deleteExpiredNotifications(@Param("now") LocalDateTime now);

    // 4. 유저당 최대 100건 유지를 위한 초과 데이터 삭제 처리에 활용
    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
}
```
**설계 이유:** 요구사항 3.2 항목에 따른 데이터 보존 정책(30일 경과 자동 삭제, 최대 100건 제한)을 완수하기 위해 백그라운드 태스크에서 사용할 만료 삭제 쿼리를 제공합니다.

### 3.2 NotificationPreferenceRepository
```java
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByMemberId(Long memberId);
}
```
**설계 이유:** 푸시 메시지 발송 결정 직전에, 대상 유저가 해당 카테고리에 수신을 동의했는지 빠르게 확인할 수 있는 단건 조회 기능입니다.

### 3.3 DiaryLikeMilestoneStateRepository
```java
public interface DiaryLikeMilestoneStateRepository extends JpaRepository<DiaryLikeMilestoneState, Long> {
    Optional<DiaryLikeMilestoneState> findByDiaryId(Long diaryId);
}
```
**설계 이유:** 개별 일지 좋아요 액션 발생 시, 마지막 알림 마일스톤에 도달했는지 확인 및 상태를 갱신하기 위해 일지 ID로 조회합니다.

---

## 4. Enum 설계 (참고용)

가이드라인에 따라 패키지명은 `com.odos.odos_server_v2.domain.notification.enums`로 수정해야 합니다 (기존 `Enum`은 자바 예약어이기도 하며, 소문자 네이밍을 준수하지 않음).

**NotificationCategory:**
- `FRIEND`
- `DIARY`
- `CHALLENGE`

**NotificationType:**
- **친구:** `FRIEND_REQUEST`, `FRIEND_ACCEPT`
- **일지:** `FRIEND_DIARY_CREATED`, `MY_DIARY_COMMENTED`, `MY_COMMENT_REPLIED`, `DIARY_LIKE_MILESTONE`
- **챌린지:** `CHALLENGE_APPLIED`, `CHALLENGE_APPROVED`, `CHALLENGE_REJECTED`
