package com.odos.odos_server_v2.domain.challenge.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "challenge")
public class Challenge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String title;

  @Column
  private @Enumerated(EnumType.STRING) Category category;

  @Column private LocalDate startDate;

  @Column private LocalDate endDate;

  @Column private boolean allowMidJoin;

  @Column private long maxParticipantsCnt;

  @Column
  @Enumerated(EnumType.STRING)
  private ChallengeType type;

  @Column private String description;

  @Column private String thumbnailImage;

  @Column @CreatedDate private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "host_member_id")
  private Member hostMember;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
  private List<Participant> participants;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
  private List<ChallengeLike> likes;

  @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
  private List<Diary> diaries;

  public void updateTitle(String title) {
    this.title = title;
  }

  public void updateThumbnailImage(String thumbnailImage) {
    this.thumbnailImage = thumbnailImage;
  }

  public void updateCategory(Category category) {
    this.category = category;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public void updateAllowMidJoin(boolean allowMidJoin) {
    this.allowMidJoin = allowMidJoin;
  }

  public void updateMaxParticipantCnt(Integer maxParticipantCnt) {
    this.maxParticipantsCnt = maxParticipantCnt;
  }

  public void setHostMember(Member member) {
    this.hostMember = member;
  }
}
