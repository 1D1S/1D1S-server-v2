package com.odos.odos_server_v2.domain.friend.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "friend",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_friend_member_friend",
          columnNames = {"member_id", "friend_member_id"})
    })
public class Friend {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "friend_member_id", nullable = false)
  private Member friendMember;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
