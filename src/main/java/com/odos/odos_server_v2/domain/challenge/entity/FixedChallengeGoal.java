package com.odos.odos_server_v2.domain.challenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고정목표(FIXED) 챌린지에 매달린 "챌린지의 목표"를 챌린지 단위로 보관한다. 참여자별 목표/일지 연결은 {@link ChallengeGoal} 이 그대로 담당하며,
 * 참여자가 들어올 때 이 테이블의 목표를 참여자의 {@link ChallengeGoal} 로 복제한다.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "fixed_challenge_goal")
@Builder
public class FixedChallengeGoal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String content;

  @ManyToOne
  @JoinColumn(name = "challenge_id")
  private Challenge challenge;
}
