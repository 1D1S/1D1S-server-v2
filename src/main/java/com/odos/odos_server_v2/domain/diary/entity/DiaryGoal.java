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
@Table(name = "DiaryGoal")
public class DiaryGoal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private Boolean isCompleted = false; // 기본값 false로 설정

  @ManyToOne
  @JoinColumn(name = "diaryId")
  private Diary diary;

  @ManyToOne
  @JoinColumn(name = "challengeGoalId")
  private ChallengeGoal challengeGoal;

  //    @ManyToOne
  //    @JoinColumn(name = "participantId")
  //    private Participant participant;

}
