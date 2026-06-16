package com.odos.odos_server_v2.domain.challenge.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "challenge_poke",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_challenge_poke_daily",
          columnNames = {"challenge_id", "actor_member_id", "receiver_member_id", "poked_date"})
    },
    indexes = {
      @Index(
          name = "idx_challenge_poke_receiver_date",
          columnList = "receiver_member_id, poked_date")
    })
public class ChallengePoke extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "challenge_id", nullable = false)
  private Challenge challenge;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_member_id", nullable = false)
  private Member actor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_member_id", nullable = false)
  private Member receiver;

  @Column(name = "poked_date", nullable = false)
  private LocalDate pokedDate;
}
