package com.odos.odos_server_v2.domain.vote.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "vote_response")
public class VoteResponse extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vote_id", nullable = false)
  private Vote vote;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Builder.Default
  @ManyToMany
  @JoinTable(
      name = "vote_response_selection",
      joinColumns = @JoinColumn(name = "response_id"),
      inverseJoinColumns = @JoinColumn(name = "option_id"))
  private List<VoteOption> selectedOptions = new ArrayList<>();

  public static VoteResponse create(Vote vote, Member member, List<VoteOption> options) {
    return VoteResponse.builder().vote(vote).member(member).selectedOptions(options).build();
  }
}
