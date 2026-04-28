package com.odos.odos_server_v2.domain.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notification_id")
  private Long id;

  @Column(nullable = false)
  private Long receiverId;

  @Column(nullable = false)
  private Long senderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  private Long targetId;

  private Integer numericValue;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  public void read() {
    this.isRead = true;
  }
}
