package com.odos.odos_server_v2.domain.diary.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.diary.dto.DiaryRequest;
import com.odos.odos_server_v2.domain.diary.entity.Enum.Feeling;
import com.odos.odos_server_v2.domain.member.entity.Member;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
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
@Table(name = "diary")
public class Diary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column private String title;

  @CreatedDate
  @Column(updatable = false)
  private LocalDate createdDate;

  @Column private LocalDate completedDate;

  @Column
  @Enumerated(EnumType.STRING)
  private Feeling feeling;

  @Column private Boolean isPublic = true;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column private Boolean isAllGoalsCompleted = false;

  @Column private Boolean isDeleted;

  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
  private List<DiaryGoal> diaryGoals = new ArrayList<>();

  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
  private List<DiaryImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
  private List<DiaryLike> likes = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "challenge_id")
  private Challenge challenge;

  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
  private List<DiaryReport> reports = new ArrayList<>();

  public void updateDiary(DiaryRequest request /*, Challenge challenge, List<DiaryGoal> goal*/) {
    this.title = request.getTitle();
    this.feeling = request.getFeeling();
    this.content = request.getContent();
    this.isPublic = request.getIsPublic();
    //    this.challenge = challenge;
    //    this.diaryGoals = goal;
  }

  public void addLike(DiaryLike diaryLike) {
    this.likes.add(diaryLike);
  }

  public void addDiaryGoal(DiaryGoal diaryGoal) {
    this.diaryGoals.add(diaryGoal);
  }
}
