# 알림(Notification) 도메인 엔티티 및 레포지토리 설계안

사용자님의 요구사항과 프로젝트의 시스템 설계 가이드라인을 종합하여, 알림 파트에 대한 엔티티 및 레포지토리 설계안을 제안합니다. 현재 코드베이스에 이미 일부 기능이 구현되어 있으나, 가이드라인(메시지 포맷팅 분리, 패키지 네이밍 등)을 준수하도록 리팩토링 및 확장하는 방향으로 설계했습니다.

---

## 1. 패키지 구조 개선

- **수정안:** `com.odos.odos_server_v2.domain.notification.entity.Enum` 패키지 이름을 `enums`로 변경합니다.
- **이유:** Java 네이밍 컨벤션과 가이드라인에 따라 패키지명에 대문자 단어를 피하고 완전한 소문자를 사용해야 하기 때문입니다.

---

## 2. 엔티티(Entity) 설계

### 2.1 Notification (알림 단건) & NotificationEvent (알림 이벤트)
알림 내역을 직접 관리하고, 같은 알림("N명 외...")을 묶어 처리할 수 있도록 설계합니다.

- **설계 방향**
  - **동적 메시지 생성 (가이드라인 핵심):** 알림 텍스트(`[닉네임]님이 댓글을 달았습니다`)를 DB에 미리 포맷팅하여 저장하지 않습니다.
  - 대신, 알림을 발생시킨 사람(`actor`), 알림 종류(`type`), 타겟 객체(`targetId`), 그룹 카운트(`groupedCount`)와 같은 메타데이터만 저장합니다.
  - API 응답을 변환하는 DTO(혹은 별도 Presentation 클래스)에서 이 데이터를 조합하여 메시지를 동적으로 생성합니다. (단, 챌린지 이름이나 일지 제목처럼 타겟이 삭제되었을 때를 대비해 필요한 스냅샷 데이터는 필드로 별도 저장할 수 있습니다. 예를 들어 `targetSnapshotData`)

- **주요 필드**
  - `Member receiver`: 수신자 (LAZY 로딩)
  - `Member actor`: 알림 행위자
  - `NotificationCategory category`: 대분류 (FRIEND, DIARY, CHALLENGE - 설정 On/Off 확인용)
  - `NotificationType type`: 소분류 (FRIEND_REQUEST, DIARY_COMMENT 등)
  - `NotificationTargetType targetType` / `Long targetId`: 느슨한 결합을 위한 랜딩용 ID
  - `String targetSnapshotData` (추가 제안): 일지 제목, 챌린지 이름 등 메시지 조합에 필요한 문자열 데이터 (DB 메시지 하드코딩 회피용)
  - `Boolean isRead`, `LocalDateTime readAt`: 읽음 상태
  - `Integer groupedCount`: 그룹화된 알림 수 (예: 5명이면 "N명 외 4명...")
  - `LocalDateTime expiresAt`: 30일 보존 기한 관리를 위한 만료 시간

### 2.2 NotificationPreference (알림 수신 설정)
유저가 앱 환경설정에서 알림 종류별(친구, 일지, 챌린지)로 수신 여부를 결정합니다.

- **설계 방향**
  - `Member`와 1:1 매핑. 회원 가입 시 기본값으로 생성됩니다.
- **주요 필드**
  - `Boolean pushEnabled`: 전체 푸시 알림 수신 여부
  - `Boolean friendEnabled`, `diaryEnabled`, `challengeEnabled`: 카테고리별 수신 여부

### 2.3 DiaryLikeMilestoneState (좋아요 알림 마일스톤 관리)
"특정 수치(1, 5, 10, 20...1000) 달성 시에만 알림"을 보내기 위한 상태 테이블입니다.

- **설계 방향**
  - 일지(`Diary`)와 1:1 매핑. 좋아요마다 알림을 보내지 않도록 마지막으로 알림이 발송된 마일스톤 단계를 기록합니다.
- **주요 필드**
  - `Diary diary`: 대상 일지
  - `Integer lastNotifiedMilestone`: 마지막 알림 발생 기준점 (예: 20까지 도달했으면 20 저장)

---

## 3. 레포지토리(Repository) 설계

### 3.1 NotificationRepository
- `Page<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver, Pageable pageable);`
  - 알림 센터에서 알림 목록을 최신순으로 페이징 조회.
- `long countByReceiverAndIsReadFalse(Member receiver);`
  - 읽지 않은 알림 배지 개수 표시.
- `Optional<Notification> findFirstByReceiverAndTypeAndTargetIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(...)`
  - 댓글 등 **알림 그룹화**를 위해, 최근 지정된 시간 내에 발생한 동일 타겟의 알림을 조회 (이후 `groupedCount`를 증가시킴).
- `void deleteByExpiresAtBefore(LocalDateTime now);`
  - `@Scheduled` 스케줄러와 연계하여 30일(혹은 최대 100건 조건 등)이 지난 알림을 일괄 삭제하기 위함.
- `int markAllAsRead(@Param("receiver") Member receiver, @Param("readAt") LocalDateTime readAt);`
  - "모두 읽음" 처리용 벌크 업데이트.

### 3.2 NotificationPreferenceRepository
- `Optional<NotificationPreference> findByMember(Member member);`
  - 특정 액션이 일어났을 때, 푸시를 발송하기 전 해당 유저가 알림을 켜두었는지 빠르게 검증.

### 3.3 DiaryLikeMilestoneStateRepository
- `Optional<DiaryLikeMilestoneState> findByDiary(Diary diary);`
  - 좋아요 수가 늘어났을 때, 마일스톤에 진입했는지 현재 상태를 확인하고 갱신하기 위함.

---

### 진행 의사 확인
위 설계안은 요구사항에 맞춰 알림 정책(그룹화, 마일스톤, 설정, 자동 삭제)을 모두 충족하며, **"DB에 동적 메시지를 사전 포맷팅하여 저장하지 않는다"**는 주요 시스템 규칙을 따랐습니다.

이 설계안이 마음에 드신다면 알려주세요. 이후부터 해당 설계에 맞추어 실제 코드로 구현 및 리팩토링을 진행하겠습니다!