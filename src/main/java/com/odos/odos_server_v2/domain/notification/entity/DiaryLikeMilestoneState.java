package com.odos.odos_server_v2.domain.notification.entity;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "diary_like_milestone_state")
public class DiaryLikeMilestoneState {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diary_id", nullable = false, unique = true)
  private Diary diary;

  @Builder.Default
  @Column(name = "last_notified_milestone", nullable = false)
  private Integer lastNotifiedMilestone = 0;

  public void updateLastNotifiedMilestone(int milestone) {
    this.lastNotifiedMilestone = milestone;
  }
}
