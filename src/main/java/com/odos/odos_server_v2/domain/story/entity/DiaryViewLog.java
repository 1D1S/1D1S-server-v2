package com.odos.odos_server_v2.domain.story.entity;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
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
    name = "diary_view_log",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_diary_view_log_member_diary",
          columnNames = {"member_id", "diary_id"})
    },
    indexes = {
      @Index(name = "idx_diary_view_log_member_diary", columnList = "member_id, diary_id")
    })
public class DiaryViewLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diary_id", nullable = false)
  private Diary diary;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
