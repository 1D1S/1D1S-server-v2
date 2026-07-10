package com.odos.odos_server_v2.domain.diary.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "diary",
    indexes = {
      @Index(
          name = "idx_diary_member_created_at_is_deleted",
          columnList = "member_id, created_at, is_deleted"),
      @Index(name = "idx_diary_member_completed_date", columnList = "member_id, completed_date")
    })
public class Diary extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String title;

  // 대표 썸네일. images(DiaryImage.url) 중 하나의 값이며, 없으면 null.
  @Column private String thumbnailUrl;

  @Column private LocalDate completedDate;

  @Column
  @Enumerated(EnumType.STRING)
  private Feeling feeling;

  // FIX
  @Builder.Default
  @Column(nullable = false)
  private Boolean isPublic = true;

  @Column(columnDefinition = "TEXT")
  private String content;

  // FIX
  @Builder.Default
  @Column(nullable = false)
  private Boolean isAllGoalsCompleted = false;

  @Builder.Default @Column private Boolean isDeleted = false;

  @Column private String deletedReason;

  @Builder.Default
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DiaryGoal> diaryGoals = new ArrayList<>();

  @Builder.Default
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DiaryImage> images = new ArrayList<>();

  @Builder.Default
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
  private List<DiaryLike> likes = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "challenge_id")
  private Challenge challenge;

  @Builder.Default
  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
  private List<DiaryReport> reports = new ArrayList<>();

  public void updateDiary(DiaryRequest request, Challenge challenge, List<DiaryGoal> diaryGoals) {
    this.title = request.getTitle();
    this.feeling = request.getFeeling();
    this.content = request.getContent();
    this.isPublic = request.getIsPublic();
    this.completedDate = request.getAchievedDate();
    this.challenge = challenge;
    this.diaryGoals.clear();
    this.diaryGoals.addAll(diaryGoals);
  }

  public void addLike(DiaryLike diaryLike) {
    this.likes.add(diaryLike);
  }

  public void addDiaryGoal(DiaryGoal diaryGoal) {
    this.diaryGoals.add(diaryGoal);
  }

  public void addDiaryImage(DiaryImage diaryImage) {
    this.images.add(diaryImage);
  }

  // imageUrls로 이미지를 전체 교체(clear-and-replace).
  // null = 변경 안 함(기존 이미지 유지), 빈 배열 = 전부 삭제.
  public void replaceImages(List<String> imageUrls) {
    if (imageUrls == null) {
      return;
    }
    this.images.clear();
    for (String url : imageUrls) {
      this.images.add(DiaryImage.builder().diary(this).url(url).build());
    }
  }

  public void updateThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void updateIsAllGoalsCompleted(Boolean isChecked) {
    this.isAllGoalsCompleted = isChecked;
  }

  public void softDelete() {
    this.isDeleted = true;
  }

  public void restore() {
    this.isDeleted = false;
  }

  public void updateDeletedReason(String reason) {
    this.deletedReason = reason;
  }
}
