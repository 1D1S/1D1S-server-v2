# 알림(Notification) 도메인 설계 문서

본 문서는 요구사항 및 시스템 설계 원칙(Memory 가이드라인)을 바탕으로 알림 도메인의 엔티티 및 레포지토리 설계를 정의합니다.

## 1. 엔티티 (Entities) 설계

### 1.1 Notification (알림 기본 엔티티)
알림의 핵심 정보를 저장하는 메인 엔티티입니다.

- **필드 설계:**
  - `Long id`: 알림 PK (Auto Increment)
  - `Long receiverId`: 알림 수신자 ID (느슨한 결합을 위해 `Member` 객체 대신 ID 사용)
  - `Long actorId`: 알림 발생자 ID (알림을 유발한 유저 ID, 시스템 알림의 경우 null)
  - `NotificationCategory category`: 카테고리 (FRIEND, DIARY, CHALLENGE 등 - 알림 설정과 연동)
  - `NotificationType type`: 알림 상세 타입 (FRIEND_REQUEST, FRIEND_ACCEPT, FRIEND_DIARY_CREATED, MY_DIARY_COMMENTED, MY_COMMENT_REPLIED, DIARY_LIKE_MILESTONE, CHALLENGE_APPLIED, CHALLENGE_REJECTED 등)
  - `NotificationTargetType targetType`: 알림이 가리키는 리소스의 타입 (PROFILE, DIARY, COMMENT, CHALLENGE 등)
  - `Long targetId`: 클릭 시 이동할 랜딩 페이지의 타겟 리소스 ID
  - `boolean isRead`: 읽음 여부 (기본값: false, `@Builder.Default` 어노테이션 사용)
  - `LocalDateTime readAt`: 읽은 시간
  - `Integer groupedCount`: 그룹화된 알림 건수 (예: "외 N명이 댓글을 달았습니다" 처리를 위함)
  - `LocalDateTime expiresAt`: 만료 시간 (알림 자동 삭제 기준 30일 설정 용도)
  - *(상속)* `createdAt`, `updatedAt`: `BaseTimeEntity` 상속을 통해 감사(Auditing) 필드 자동 관리

- **설계 이유:**
  - **동적 메시지 생성:** 가이드라인에 따라 데이터베이스에 사전 포맷팅된 메시지(`message` 문자열)를 직접 저장하지 않습니다. 대신 `actorId`, `type`, `targetId`, `groupedCount` 등의 데이터만 저장하고, API 응답(DTO 변환 단계) 시 클라이언트 측 혹은 서버에서 알림 메시지를 동적으로 생성하도록 설계했습니다. 이는 다국어 지원 및 UI 텍스트 변경 시의 유연성을 크게 높여줍니다.
  - **느슨한 결합(Loose Coupling):** `Member`, `Diary`, `Challenge` 등의 타 도메인 엔티티를 `@ManyToOne`으로 직접 참조하지 않고 `Long receiverId`, `Long targetId` 등 원시 타입(래퍼) ID를 사용하여 도메인 간 결합도를 낮췄습니다. (JPA 연관관계 매핑 최소화)
  - **알림 그룹화:** 짧은 시간에 동일 게시물에 여러 댓글이 달릴 경우, `groupedCount`를 활용하여 개별 알림을 생성하지 않고 기존 알림을 효율적으로 병합할 수 있도록 했습니다.

### 1.2 NotificationPreference (알림 설정 엔티티)
유저별로 수신할 알림 카테고리를 On/Off 하는 설정 데이터를 관리합니다.

- **필드 설계:**
  - `Long id`: 설정 PK
  - `Long memberId`: 유저 ID (느슨한 결합)
  - `boolean friendEnabled`: 친구 관련 알림 수신 여부 (`@Builder.Default` true)
  - `boolean diaryEnabled`: 일지 및 커뮤니티 알림 수신 여부 (`@Builder.Default` true)
  - `boolean challengeEnabled`: 챌린지 알림 수신 여부 (`@Builder.Default` true)
  - *(상속)* `createdAt`, `updatedAt`: `BaseTimeEntity` 상속

- **설계 이유:**
  - 기획 요구사항의 "[알림 설정] 페이지에서 카테고리별(친구/일지/챌린지) On/Off 기능 제공"을 만족하기 위해, 알림 발송 전 이 엔티티를 조회하여 푸시 발송 여부를 결정할 수 있도록 별도로 분리했습니다.

### 1.3 DiaryLikeMilestoneState (좋아요 마일스톤 관리 엔티티)
특정 일지의 좋아요 누적 수치를 관리하여, 동일한 일지에 대해 중복 알림이 발송되는 것을 방지합니다.

- **필드 설계:**
  - `Long id`: PK
  - `Long diaryId`: 대상 일지 ID (느슨한 결합)
  - `int lastNotifiedCount`: 마지막으로 마일스톤 알림이 발송된 좋아요 개수 (1, 5, 10, 20, 50, 100, 1000...)

- **설계 이유:**
  - "1개 / 5개 / 10개..." 등 특정 누적 수치 달성 시에만 알림을 발송하는 기획 요건을 구현하기 위해 일지별로 어디까지 알림을 보냈는지의 상태를 관리합니다. `Diary` 엔티티를 수정하지 않고 알림 도메인 내에서 상태를 독립적으로 관리하기 위함입니다.

### 1.4 Enums (열거형) 설계 정책
- 모든 열거형(Enum) 파일은 자바 네이밍 규칙(Lowercase 패키지명)을 준수하여 `com.odos.odos_server_v2.domain.notification.enums` 패키지에 위치하도록 설계합니다. (기존 대문자 `Enum` 패키지는 사용하지 않습니다.)
- 주요 Enum 목록:
  - `NotificationCategory` (FRIEND, DIARY, CHALLENGE 등)
  - `NotificationType` (FRIEND_REQUEST, DIARY_LIKE_MILESTONE, FRIEND_DIARY_CREATED 등)
  - `NotificationTargetType` (PROFILE, DIARY, COMMENT, CHALLENGE 등)

---

## 2. 레포지토리 (Repositories) 설계

### 2.1 NotificationRepository
- **메서드:**
  - `Page<Notification> findByReceiverId(Long receiverId, Pageable pageable)`: 특정 유저의 알림 목록을 최신순으로 조회합니다.
  - `Optional<Notification> findTopByReceiverIdAndTargetTypeAndTargetIdAndTypeOrderByCreatedAtDesc(...)`: 그룹화 대상이 되는 최신 알림 단건을 조회하여 병합(Grouped)이 가능한지 확인합니다.
  - `void deleteByExpiresAtBefore(LocalDateTime now)`: 만료 시간(30일)이 지난 알림을 벌크 삭제합니다. (Spring `@Scheduled` 배치/스케줄러와 연동)
  - `@Query(...) void deleteOldNotificationsToKeepLimit(Long receiverId, int limit)`: 최대 100건 제한을 초과하는 오래된 알림을 삭제하는 서브쿼리 기반의 커스텀 쿼리를 제공합니다.

- **설계 이유:**
  - 데이터 보존 기한 정책("최근 30일 혹은 최대 100건까지만 저장 후 자동 삭제")을 처리하기 위한 자동화된 쿼리가 필요합니다. 향후 백그라운드 스케줄러가 주기적으로 이 레포지토리의 삭제 메서드를 호출하여 데이터를 정리하게 됩니다.

### 2.2 NotificationPreferenceRepository
- **메서드:**
  - `Optional<NotificationPreference> findByMemberId(Long memberId)`: 특정 유저의 알림 설정을 조회합니다.
- **설계 이유:**
  - 알림 이벤트를 컨슘하거나 발송하기 직전에 수신자의 동의 여부(알림 On/Off)를 빠르게 조회하기 위해 사용됩니다. 유저 1명당 1개의 설정 정보만 매핑됩니다.

### 2.3 DiaryLikeMilestoneStateRepository
- **메서드:**
  - `Optional<DiaryLikeMilestoneState> findByDiaryId(Long diaryId)`: 일지 ID로 현재까지 도달한 마일스톤 알림 상태를 조회합니다.
- **설계 이유:**
  - 좋아요 이벤트 발생 시, 해당 일지의 현재 좋아요 개수가 새로운 마일스톤 임계치(예: 50개 -> 100개)에 도달했는지 판별하기 위해 기존 알림 발송 상태를 조회하고 업데이트합니다.
