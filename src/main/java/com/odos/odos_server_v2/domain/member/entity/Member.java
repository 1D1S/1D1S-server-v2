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
import org.springframework.data.annotation.CreatedDate;
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
          columnNames = {"email", "signup_route"}),
      // 전화번호 중복 불가. NULL 은 Postgres 기본 동작상 서로 distinct 하여 다중 허용.
      @UniqueConstraint(
          name = "uk_member_phone_number",
          columnNames = {"phone_number"})
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

  // 상품 발송용. 저장 형식은 숫자만(하이픈 제거). 기존 회원은 null 가능.
  @Column(name = "phone_number")
  private String phoneNumber;

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

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

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
      Boolean isPublic,
      String phoneNumber) {
    this.nickname = nickname;
    this.profileUrl = profileImageKey;
    this.job = job;
    this.birth = birth;
    this.gender = gender;
    this.isPublic = isPublic;
    this.phoneNumber = normalizePhoneNumber(phoneNumber);
    this.role = MemberRole.USER;
    this.nicknameLastModifiedAt = LocalDateTime.now();
  }

  public void updatePhoneNumber(String phoneNumber) {
    this.phoneNumber = normalizePhoneNumber(phoneNumber);
  }

  // 하이픈 등 숫자 외 문자를 제거해 숫자만으로 통일 저장한다(형식 검증은 요청 DTO에서 수행).
  // 저장값과 동일한 형태로 중복 조회하기 위해 서비스에서도 재사용한다.
  public static String normalizePhoneNumber(String raw) {
    return raw == null ? null : raw.replaceAll("[^0-9]", "");
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

  public void softDelete() {
    this.status = MemberStatus.DELETED;

    String dummySuffix = this.id + "_" + System.currentTimeMillis();

    this.email = "deleted_" + dummySuffix + "@deleted.local";
    this.socialId = "deleted_" + dummySuffix;
    this.nickname = "탈퇴한 사용자";

    this.refreshToken = null;
  }

  public void restore() {
    this.status = MemberStatus.ACTIVE;
    this.deletedAt = null;
  }

  public void updateAdminRole() {
    this.role = MemberRole.ADMIN;
  }

  /** 소셜 로그인 이후 필수 프로필 정보가 모두 채워졌는지 여부. */
  public boolean isProfileComplete() {
    return nickname != null && job != null && birth != null && gender != null && isPublic != null;
  }
}
