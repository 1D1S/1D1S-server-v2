package com.odos.odos_server_v2.domain.vote.entity;

import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "vote")
public class Vote extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "selection_type", nullable = false)
  private VoteSelectionType selectionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "vote_type", nullable = false)
  private VoteType voteType;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Builder.Default
  @OrderBy("displayOrder asc")
  @OneToMany(
      mappedBy = "vote",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<VoteOption> options = new ArrayList<>();

  public static Vote create(
      String title,
      VoteSelectionType selectionType,
      VoteType voteType,
      LocalDate startDate,
      LocalDate endDate,
      List<String> optionTexts) {
    Vote vote =
        Vote.builder()
            .title(title)
            .selectionType(selectionType)
            .voteType(voteType)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    for (int i = 0; i < optionTexts.size(); i++) {
      vote.options.add(VoteOption.create(vote, optionTexts.get(i), i));
    }
    return vote;
  }

  public boolean isOpenOn(LocalDate date) {
    return !date.isBefore(startDate) && !date.isAfter(endDate);
  }
}
