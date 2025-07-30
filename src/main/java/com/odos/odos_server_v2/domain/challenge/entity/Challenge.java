package com.odos.odos_server_v2.domain.challenge.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table(name = "Challenge")
public class Challenge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String title;

  @Column private Category category;

  @Column private LocalDate startDate;

  @Column private LocalDate endDate;

  @Column private int participantsCnt;

  @Column
  @Enumerated(EnumType.STRING)
  private ChallengeType type;

  @Column private String description;

  @ManyToOne
  @JoinColumn(name = "challengeHost")
  private Member hostMember;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
  private List<Participant> memberChallenges;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
  private List<ChallengeLike> likes;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
  private List<Diary> diaries;
}
