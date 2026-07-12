package com.odos.odos_server_v2.domain.challenge.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Enum.ChallengeType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.GoalType;
import com.odos.odos_server_v2.domain.challenge.entity.Enum.ParticipationType;
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

  // 인증샷(사진) 필수 여부. true면 일지 작성 시 이미지 1장 이상 필수.
  @Column(nullable = false)
  private boolean photoRequired;

  // 챌린지 종료 후 유예 기간(종료일+2일, KST) 내 일지 작성 허용 여부. false면 종료 즉시 작성 불가.
  @Column(nullable = false)
  private boolean postEndWriteAllowed;

  @Column private Long maxParticipantsCnt;

  @Column
  @Enumerated(EnumType.STRING)
  private GoalType goalType;

  @Column
  @Enumerated(EnumType.STRING)
  private ParticipationType participationType;

  @Column private String description;

  @Column private String thumbnailImage;

  @Column
  @Enumerated(EnumType.STRING)
  private ChallengeType challengeType;

  @Column private String password;

  @Column @CreatedDate private LocalDateTime createdAt;

  // 공식(OFFICIAL) 챌린지 예약 노출 시각(KST 벽시계). null=즉시 노출. 이 시각 이전이면 클라 조회에서 제외.
  @Column private LocalDateTime visibleFrom;

  @Column private LocalDateTime deletedAt;

  @Column private String deletedReason;

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

  public void updateMaxParticipantCnt(Long maxParticipantCnt) {
    this.maxParticipantsCnt = maxParticipantCnt;
  }

  public void updateChallengeType(ChallengeType challengeType) {
    this.challengeType = challengeType;
  }

  public void updatePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void updateVisibleFrom(LocalDateTime visibleFrom) {
    this.visibleFrom = visibleFrom;
  }

  public void setHostMember(Member member) {
    this.hostMember = member;
  }

  public void updateDeletedReason(String reason) {
    this.deletedReason = reason;
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void restore() {
    this.deletedAt = null;
  }
}
