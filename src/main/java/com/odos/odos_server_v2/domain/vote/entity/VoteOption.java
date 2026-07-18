package com.odos.odos_server_v2.domain.vote.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "vote_option")
public class VoteOption {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vote_id", nullable = false)
  private Vote vote;

  @Column(name = "option_text", nullable = false)
  private String text;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  static VoteOption create(Vote vote, String text, int displayOrder) {
    return VoteOption.builder().vote(vote).text(text).displayOrder(displayOrder).build();
  }
}
