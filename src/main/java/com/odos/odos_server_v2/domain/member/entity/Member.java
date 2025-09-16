package com.odos.odos_server_v2.domain.member.entity;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.entity.ChallengeLike;
import com.odos.odos_server_v2.domain.challenge.entity.Participant;
import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.diary.entity.DiaryLike;
import com.odos.odos_server_v2.domain.diary.entity.DiaryReport;
import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
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
@Table(name = "member")
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
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

  @OneToMany(mappedBy = "hostMember", cascade = CascadeType.ALL)
  private List<Challenge> challenges;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Participant> participants;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<ChallengeLike> challengeLikes;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Interest> memberInterests = new ArrayList<>();

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<DiaryLike> diaryLikes;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<DiaryReport> diaryReports;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  private List<Diary> diaries;

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
    this.profileUrl = profileUrl;
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
}
