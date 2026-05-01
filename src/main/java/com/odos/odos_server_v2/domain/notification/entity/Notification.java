package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.notification.entity.enums.NotificationType;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long receiverId;

  @Column private Long senderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column private Long targetId;

  @Column private Integer milestoneCount;

  @Builder.Default
  @Column(nullable = false)
  private boolean isRead = false;
}
