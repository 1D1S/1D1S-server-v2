package com.odos.odos_server_v2.domain.diary.entity;

import com.odos.odos_server_v2.domain.challenge.entity.ChallengeGoal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table(name = "diary_goal")
public class DiaryGoal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // FIX: Builder.Default로 기본값 설정
  @Builder.Default
  @Column(nullable = false)
  private Boolean isCompleted = false;

  @ManyToOne
  @JoinColumn(name = "diary_id")
  private Diary diary;

  @ManyToOne
  @JoinColumn(name = "challenge_goal_id")
  private ChallengeGoal challengeGoal;

  //    @ManyToOne : 나중에 필요하면 칼럼에 넣기 (의논후 주석 빼기)
  //    @JoinColumn(name = "participantId")
  //    private Participant participant;

}
