# 알림(Notification) 기능 엔티티 및 레포지토리 설계 문서

이 문서는 새롭게 요구된 알림 기능 기획에 맞춘 도메인 엔티티와 레포지토리의 설계를 담고 있습니다.

## 1. Enum 설계

Java 명명 규칙에 따라 `Enum`이라는 대문자 패키지명 대신 `enums`라는 소문자 패키지명을 사용합니다.

### 1.1 `NotificationType` (알림 타입)
각각의 알림 이벤트를 정의합니다.
* `FRIEND_REQUEST` (친구 신청)
* `FRIEND_ACCEPT` (친구 수락)
* `FRIEND_DIARY_CREATED` (친구 일지 등록)
* `MY_DIARY_COMMENTED` (내 일지 댓글)
* `MY_COMMENT_REPLIED` (내 댓글 대댓글)
* `DIARY_LIKE_MILESTONE` (좋아요 마일스톤)
* `CHALLENGE_APPLIED` (챌린지 신청/승낙)
* `CHALLENGE_REJECTED` (챌린지 거절)

### 1.2 `NotificationCategory` (알림 카테고리 설정용)
푸시 설정에서 On/Off를 제어할 수 있도록 큰 범주를 정의합니다.
* `FRIEND` (친구)
* `DIARY` (일지)
* `CHALLENGE` (챌린지)

### 1.3 `NotificationTargetType` (랜딩 타겟 타입)
알림 클릭 시 이동할 랜딩 페이지의 도메인을 정의합니다.
* `MEMBER` (유저 프로필 페이지)
* `DIARY` (일지 상세 페이지)
* `CHALLENGE` (챌린지 상세 화면)

## 2. Entity 설계

모든 엔티티는 `BaseTimeEntity`를 상속받아 `createdAt`, `updatedAt` 등의 감사(Auditing) 필드를 기본으로 가집니다.

### 2.1 `Notification` (알림)
발송된 알림의 내역을 저장하는 엔티티입니다.

**주요 필드 및 이유:**
* `Long id`: PK
* `Long receiverId`: 알림을 받는 유저의 ID. (타 도메인 엔티티 직접 참조를 피하고 느슨한 결합을 유지하기 위해 `@ManyToOne` 대신 원시/래퍼 타입 ID 사용)
* `Long actorId`: 알림을 발생시킨 유저의 ID (ex. 댓글을 단 사람, 친구 신청을 한 사람).
* `NotificationType type`: 알림의 상세 타입.
* `NotificationCategory category`: 알림 카테고리 (설정 온오프 확인용).
* `String dynamicData`: JSON 형식 등의 문자열로 변수 데이터(예: 닉네임, 일지 제목, N개, 그룹화된 유저 수)를 저장합니다. **DB에 사전 포맷팅된 완성형 메시지 문자열을 저장하지 않고, API 응답 시점에 동적으로 메시지를 생성하기 위해** 필요한 원본 데이터만 저장합니다.
* `NotificationTargetType targetType`: 랜딩 시 필요한 타겟 도메인 구분.
* `Long targetId`: 랜딩 시 필요한 타겟 도메인의 ID (유저 ID, 일지 ID, 챌린지 ID 등). 느슨한 결합을 위해 Long 타입으로 저장.
* `Integer groupedCount`: 알림 그룹화 처리를 위한 카운트 (ex. "외 N명이 댓글을 달았습니다").
* `Boolean isRead`: 읽음 여부 (`@Builder.Default`로 기본값 `false` 설정하여 빌더 경고 방지).
* `LocalDateTime readAt`: 읽은 시간.
* `LocalDateTime expiresAt`: 알림 만료 시간. (데이터 보존 정책에 따라 30일 뒤 삭제를 위한 스케줄링 처리 시 기준이 되는 시간).

### 2.2 `NotificationPreference` (알림 푸시 설정)
유저의 카테고리별 푸시 알림 수신 여부를 저장하는 엔티티입니다.

**주요 필드 및 이유:**
* `Long id`: PK
* `Long memberId`: 설정의 주체인 유저 ID (느슨한 결합).
* `Boolean isFriendNotificationEnabled`: 친구 관련 알림 수신 여부.
* `Boolean isDiaryNotificationEnabled`: 일지 관련 알림 수신 여부.
* `Boolean isChallengeNotificationEnabled`: 챌린지 관련 알림 수신 여부.

## 3. Repository 설계

### 3.1 `NotificationRepository`
* `Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable)`: 알림 센터 조회용.
* `long countByReceiverIdAndIsReadFalse(Long receiverId)`: 안읽은 알림 배지 카운트용.
* `void deleteByExpiresAtBefore(LocalDateTime now)`: 30일 지난 알림 삭제용 스케줄러에서 사용.
* `@Query` / `@Modifying int deleteOldestNotificationsIfExceedsLimit(Long receiverId, int limit)`: 최대 100건 제한 정책을 지키기 위해, 100건을 초과하는 오래된 알림을 삭제하는 쿼리(또는 서브쿼리나 네이티브 쿼리를 활용한 배치 삭제).
* `@Modifying @Query(...) int markAllAsRead(Long receiverId)`: 알림 모두 읽음 처리.

### 3.2 `NotificationPreferenceRepository`
* `Optional<NotificationPreference> findByMemberId(Long memberId)`: 특정 유저의 알림 설정 조회.
* 푸시 발송 시, 대상 유저의 설정 값을 조회하여 On일 때만 발송 로직에 진입하도록 활용합니다.

## 4. 데이터 보존 및 정리 스케줄러 (정책 반영)
Spring의 `@Scheduled` 어노테이션을 사용하는 `NotificationCleanupService` 등의 컴포넌트를 설계하여 주기적으로 아래 두 가지 정리 로직을 실행합니다.
1. `expiresAt`이 현재 시간보다 이전인 알림들을 일괄 삭제 (30일 보존 기한).
2. 각 유저별로 최신 알림을 100개까지만 남기고 초과분 삭제 (최대 100건 저장 제한).
