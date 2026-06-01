# 알림(Notification) 도메인 설계안

요구사항과 시스템 가이드라인을 바탕으로 알림(Notification) 도메인의 엔티티, 열거형(Enum), 그리고 레포지토리를 설계했습니다.

## 1. Enum 설계: `NotificationType`

```java
package com.odos.odos_server_v2.domain.notification.enums;

public enum NotificationType {
    // 친구 관련
    FRIEND_REQUEST,       // 친구 신청
    FRIEND_ACCEPT,        // 친구 수락

    // 일지 및 커뮤니티
    FRIEND_DIARY_REGISTER,// 친구 일지 등록
    MY_DIARY_COMMENT,     // 내 일지 댓글
    MY_COMMENT_REPLY,     // 내 댓글 대댓글
    LIKE_MILESTONE,       // 좋아요 마일스톤 달성

    // 챌린지 관련
    CHALLENGE_ACCEPT,     // 챌린지 승낙
    CHALLENGE_REJECT      // 챌린지 거절
}
```

### 설계 이유
- **종류별 명확한 분리**: 알림의 종류를 Enum으로 분리하여 관리함으로써, 클라이언트 응답 시 동적으로 알림 메시지를 생성하거나 랜딩 페이지로 이동할 때 타입별 분기 처리가 용이해집니다.
- **Java 네이밍 컨벤션 준수**: Enum이 위치할 패키지명은 시스템 메모리의 지침에 따라 모두 소문자인 `enums`로 명명합니다.

---

## 2. Entity 설계: `Notification`

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

    // 알림을 받는 사용자 (알림의 주인)
    @Column(nullable = false)
    private Long receiverId;

    // 알림을 발생시킨 사용자 (예: 친구 신청자, 댓글 작성자 등)
    @Column(name = "sender_id")
    private Long senderId;

    // 알림의 유형
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // 관련 엔티티의 ID (랜딩용: 일지 ID, 챌린지 ID, 댓글 ID 등)
    // 다른 도메인과 느슨한 결합을 유지하기 위해 연관관계 매핑(@ManyToOne) 대신 ID를 직접 저장
    @Column(name = "target_id")
    private Long targetId;

    // 알림 읽음 여부
    @Builder.Default
    @Column(nullable = false)
    private boolean isRead = false;

    // 좋아요 마일스톤 도달 수치 등 타입별로 추가로 필요한 메타 데이터
    // 예) 1, 5, 10, 50, 100 ...
    @Column(name = "milestone_count")
    private Integer milestoneCount;
}
```

### 설계 이유
1. **완성된 메시지가 아닌 데이터 저장**: "데이터베이스에 사전 포맷팅된 동적 메시지 문자열(예: 알림 메시지)을 저장하지 말 것"이라는 가이드라인을 지키기 위해 구성되었습니다.
   대신 `senderId`(행위자), `type`(알림의 종류), `targetId`(대상 컨텐츠), `milestoneCount`(좋아요 달성 수치)만 저장한 뒤, API 응답 시점에 이 데이터들을 기반으로 조합하여 요구사항에 맞는 `[닉네임]님이 ...` 형태의 메시지를 생성합니다.
2. **느슨한 결합 (Loose Coupling)**: 타 도메인(Member, Diary, Challenge 등)과의 의존성을 최소화하기 위해 `@ManyToOne`과 같은 객체 참조 대신 기본형/래퍼 클래스인 ID(`Long receiverId`, `Long targetId` 등)를 직접 저장합니다.
3. **`BaseTimeEntity` 상속**: 시스템의 공통 감사(Audit) 필드인 `createdAt` 등을 상속받기 위해 상속 구조를 적용하였습니다.
4. **`@Builder.Default` 활용**: 필드 초기화(`isRead = false;`)에 대한 컴파일 경고를 방지하기 위해 가이드라인에 따라 `@Builder.Default`를 사용했습니다.

---

## 3. Repository 설계: `NotificationRepository`

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

    // 1. 특정 사용자의 알림 목록 조회 (최신순 정렬)
    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // 2. 알림 삭제 (보존 기한 정책: 최근 30일이 지난 알림 삭제 처리용)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
```

### 설계 이유
- **Spring Data JPA 활용**: 기본적인 CRUD 및 데이터베이스 접근을 효율적으로 지원합니다.
- **`findAllByReceiverIdOrderByCreatedAtDesc`**: 알림 센터에서 사용자에게 최신순으로 알림을 노출할 수 있도록 제공합니다.
- **데이터 보존 기한 정책 지원**: "최근 30일 혹은 최대 100건까지만 저장 후 자동 삭제" 요구사항에 맞추어 `deleteOlderThan`과 같은 삭제용 메서드를 두어 `@Scheduled` 어노테이션이 붙은 스케줄러 메서드(향후 서비스 계층에 구현)가 주기적으로 만료된 알림을 삭제할 수 있도록 설계했습니다.
