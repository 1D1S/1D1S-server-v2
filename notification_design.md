# 알림(Notification) 도메인 엔티티 및 레포지토리 설계서

본 문서는 요구사항에 맞춰 알림 시스템을 구현하기 위한 엔티티(Entity) 및 레포지토리(Repository) 설계와 그에 대한 이유(Justification)를 정리한 문서입니다.

## 1. 엔티티(Entity) 설계

알림 기능은 다수의 사용자에게 발송될 수 있고, 다양한 도메인(친구, 일지, 챌린지)과 연결되므로 **느슨한 결합도(Loose Coupling)**와 **데이터 중복 최소화**를 핵심 설계 원칙으로 삼았습니다.
모든 주요 도메인 엔티티는 공통 감사 필드(생성일 등)를 위해 `BaseTimeEntity`를 상속합니다.

### 1.1 `Notification` (알림 수신 내역)
개별 사용자가 수신하는 알림의 상태(읽음 여부 등)를 관리하는 엔티티입니다.

- **필드 구성:**
  - `id`: PK
  - `receiver` (`@ManyToOne` Member): 알림을 받는 사용자
  - `event` (`@ManyToOne` NotificationEvent): 1:N 알림 발송 시 데이터 중복 방지를 위한 공통 이벤트 참조
  - `actor` (`@ManyToOne` Member): 알림을 발생시킨 주체 (친구 신청자, 댓글 작성자 등)
  - `category` (`NotificationCategory`): 알림 대분류 (FRIEND, DIARY, CHALLENGE)
  - `type` (`NotificationType`): 알림 상세 유형 (FRIEND_REQUEST, DIARY_LIKE_MILESTONE 등)
  - `targetType` (`NotificationTargetType`): 알림 클릭 시 이동할 랜딩 페이지 도메인 타입 (MEMBER_PROFILE, DIARY_DETAIL 등)
  - `targetId` (`Long`): 랜딩 페이지에 필요한 도메인 리소스 ID (느슨한 결합을 위해 `@ManyToOne` 대신 원시 타입 ID 사용)
  - `isRead` (`Boolean`): 알림 읽음 상태 (기본값 false, `@Builder.Default` 적용)
  - `readAt` (`LocalDateTime`): 알림을 읽은 시간
  - `groupedCount` (`Integer`): 그룹화된 알림 건수 (예: "외 N명이 댓글을 달았습니다")
  - `expiresAt` (`LocalDateTime`): 알림 만료 시간 (30일 보존 기한 관리를 위해 사용)

- **설계 이유:**
  - **느슨한 결합(Loose Coupling):** `Diary`, `Challenge` 등 타 도메인 엔티티와 `@ManyToOne` 등 강한 의존성을 맺지 않고 `targetId` 기반으로 참조하여, 도메인 간 결합도를 낮췄습니다.
  - **동적 메시지 생성:** 시스템 규칙상 사전 포맷팅된 메시지(Notification text)를 DB에 저장하지 않고, `type`과 `actor` 등의 메타데이터만 저장하여 API 응답 시점에 다국어 처리나 메시지 포맷팅을 동적으로 생성하도록 설계해야 합니다. (단, 그룹화 처리 등 불가피한 경우 메타데이터로 문자열을 제한적으로 캐싱할 수 있습니다.)
  - **데이터 보존 기한:** `expiresAt` 필드를 두어 인덱스를 생성하면, `@Scheduled` 기반의 배치 작업에서 30일이 지난 알림을 효율적으로 삭제할 수 있습니다.

### 1.2 `NotificationEvent` (알림 발생 이벤트)
여러 사용자에게 동일한 알림(예: 친구 일지 등록 등 N명에게 동시 발송)을 보낼 때 중복 데이터를 방지하기 위한 공통 이벤트 엔티티입니다.

- **설계 이유:** 동일한 이벤트 하나에 대해 여러 개의 `Notification`이 수신자별로 생성될 때, 타입이나 대상 URL ID 등을 정규화하여 중복 저장을 방지하는 목적으로 사용합니다.

### 1.3 `NotificationPreference` (알림 수신 설정)
유저별로 특정 카테고리의 알림 수신(Push) 여부를 설정하는 엔티티입니다.

- **필드 구성:**
  - `id`: PK
  - `member` (`@OneToOne` Member)
  - `pushEnabled` (`Boolean`): 전체 푸시 알림 수신 여부
  - `friendEnabled` (`Boolean`): 친구 알림 수신 여부 (신청, 수락 등)
  - `diaryEnabled` (`Boolean`): 일지 알림 수신 여부 (댓글, 좋아요 등)
  - `challengeEnabled` (`Boolean`): 챌린지 알림 수신 여부 (승낙, 거절 등)

- **설계 이유:** 설정 정보는 유저 정보(`Member` 엔티티)에 직접 두기보다 별도의 1:1 매핑 엔티티로 분리하여 `Member` 테이블의 비대화를 방지하고, 알림 도메인 내에서 설정 변경 내역을 응집도 있게 관리할 수 있도록 했습니다.

### 1.4 `DiaryLikeMilestoneState` (일지 좋아요 마일스톤 상태)
특정 일지의 '좋아요 누적 마일스톤' 달성 여부를 기록하여, 중복 알림 발송을 방지하기 위한 상태 엔티티입니다.

- **필드 구성:**
  - `id`: PK
  - `diary` (`@OneToOne` Diary): 대상 일지
  - `lastNotifiedMilestone` (`Integer`): 가장 마지막으로 알림이 발송된 마일스톤 수치 (1, 5, 10, 20, 50, 100, 1000...)

- **설계 이유:** 개별 좋아요마다 알림을 보내는 것이 아니라 특정 구간별로 보내야 하므로, 이전까지 어디까지 달성되었는지를 기록해두어야 동시성 문제나 중복 알림 발송 오류를 방지할 수 있습니다. `Diary` 엔티티를 수정하지 않고 확장이 가능하도록 별도의 엔티티로 설계하였습니다.

### 1.5 Enums

- **`NotificationCategory`:** FRIEND, DIARY, CHALLENGE (설정 및 그룹화 목적)
- **`NotificationType`:** FRIEND_REQUEST, FRIEND_ACCEPT, FRIEND_DIARY_CREATED, MY_DIARY_COMMENTED, MY_COMMENT_REPLIED, DIARY_LIKE_MILESTONE, CHALLENGE_APPLIED, CHALLENGE_APPROVED, CHALLENGE_REJECTED (동적 메시지 생성의 기준 역할)
- **`NotificationTargetType`:** MEMBER_PROFILE, DIARY_DETAIL, DIARY_COMMENT, CHALLENGE_DETAIL, CHALLENGE_LIST (클릭 시 이동할 랜딩 페이지 분기용)

---

## 2. 레포지토리(Repository) 설계

Spring Data JPA를 기반으로 구성하며, 필요한 쿼리 메서드만 정의합니다.

### 2.1 `NotificationRepository`
알림 조회 및 삭제 처리를 담당합니다.
- `Page<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable)`: 내 알림 목록 최신순 조회. (최대 100건 제한은 Pageable로 처리)
- `long countByReceiverAndIsReadFalse(Member receiver)`: 읽지 않은 알림 개수 조회 (뱃지 표시용)
- `void deleteByExpiresAtBefore(LocalDateTime now)`: 30일이 지난 데이터 삭제 (스케줄러에서 사용)
- `int markAllAsRead(@Param("receiver") Member receiver, @Param("readAt") LocalDateTime readAt)`: 벌크 쿼리(`@Modifying`)를 활용한 알림 일괄 읽음 처리.
- `Optional<Notification> findFirstBy...`: 최근 알림 조회로 그룹화 처리(댓글 "외 N명")에 활용.

### 2.2 `NotificationEventRepository`
다수 대상 알림 발생 시 사용하는 공통 이벤트 저장소입니다.

### 2.3 `NotificationPreferenceRepository`
- `Optional<NotificationPreference> findByMember(Member member)`: 사용자별 알림 수신 설정 조회. 알림을 발송하기 전에 수신 여부를 판별하기 위해 활용됩니다.

### 2.4 `DiaryLikeMilestoneStateRepository`
- `Optional<DiaryLikeMilestoneState> findByDiaryId(Long diaryId)`: 좋아요가 추가될 때 해당 일지의 마일스톤 도달 여부를 확인하기 위해 조회. Lock을 적용하여 좋아요가 급증하는 경우에도 동시성 문제를 방지할 수 있도록 설계합니다. (`@Lock(LockModeType.PESSIMISTIC_WRITE)` 등 적용 가능)

---

## 3. 종합 요약
- 본 설계는 JPA 연관관계를 최소화(원시 타입 ID 사용)하여 각 도메인 간의 강결합을 방지하고 확장성을 도모합니다.
- 동적 메시지 생성 방침에 따라 본문을 통째로 저장하는 대신 **메타데이터(Type, TargetId, Actor)** 위주로 저장합니다.
- 마일스톤 달성 알림 및 설정 관리 기능은 기존 메인 도메인을 오염시키지 않도록 독립적인 엔티티(`DiaryLikeMilestoneState`, `NotificationPreference`)로 분리하였습니다.
- 보존 기한(30일) 관리를 위해 `expiresAt` 컬럼과 `@Scheduled` 조합의 삭제 전략을 설계하여 쿼리 성능 저하를 방지합니다.
