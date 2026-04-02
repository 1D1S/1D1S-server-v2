package com.odos.odos_server_v2.domain.member.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.diary.entity.DiaryReport;
import com.odos.odos_server_v2.domain.member.entity.Enum.*;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "member",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_email_signup_route",
          columnNames = {"email", "signup_route"})
    })
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "signup_route")
  private SignupRoute signupRoute;

  private String socialId;
  private String refreshToken;

  @Enumerated(EnumType.STRING)
  private MemberRole role;

  private String nickname;
  private String profileUrl;

  @Enumerated(EnumType.STRING)
  private Job job;

  private LocalDate birth;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private Boolean isPublic;

  private LocalDateTime nicknameLastModifiedAt;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatus status = MemberStatus.ACTIVE;

  private LocalDateTime deletedAt;

  @Builder.Default
  @OneToMany(mappedBy = "hostMember", cascade = CascadeType.ALL)
  private List<Challenge> challenges = new ArrayList<>();

  ;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Participant> participants = new ArrayList<>();

  ;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<ChallengeLike> challengeLikes = new ArrayList<>();

  ;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Interest> memberInterests = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<DiaryLike> diaryLikes = new ArrayList<>();

  ;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<DiaryReport> diaryReports = new ArrayList<>();

  ;

  @Builder.Default
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<Diary> diaries = new ArrayList<>();

  ;

  public void updateRefreshToken(String updateRefreshToken) {
    this.refreshToken = updateRefreshToken;
  }

  public void completeProfile(
      String nickname,
      String profileImageKey,
      Job job,
      LocalDate birth,
      Gender gender,
      Boolean isPublic) {
    this.nickname = nickname;
    this.profileUrl = profileImageKey;
    this.job = job;
    this.birth = birth;
    this.gender = gender;
    this.isPublic = isPublic;
    this.role = MemberRole.USER;
    this.nicknameLastModifiedAt = LocalDateTime.now();
  }

  public void updateCategories(List<Category> categories) {
    this.memberInterests.clear();

    for (Category category : categories) {
      this.memberInterests.add(new Interest(this, category));
    }
  }

  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }

  public void updateProfileImage(String objectKey) {
    this.profileUrl = objectKey;
  }

  public void withdraw() {
    this.status = MemberStatus.WITHDRAWN;
    this.deletedAt = LocalDateTime.now();
  }
}
