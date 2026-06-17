package com.odos.odos_server_v2.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 회원 정보 조회 응답 DTO")
public class AdminMemberResponseDto {

  @Schema(description = "회원 ID", example = "1")
  private Long memberId;

  @Schema(description = "닉네임", example = "홍길동")
  private String nickname;

  @Schema(description = "이메일", example = "user@example.com")
  private String email;

  @Schema(description = "가입한 SNS (KAKAO, NAVER, GOOGLE, APPLE)", example = "KAKAO")
  private SignupRoute signupRoute;

  @Schema(description = "가입일", example = "2025-01-15")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDateTime createdAt;

  @Schema(description = "직업 상태 (STUDENT, WORKER)", example = "STUDENT")
  private Job job;

  @Schema(description = "성별 (MALE, FEMALE, ETC)", example = "MALE")
  private Gender gender;

  @Schema(description = "관심 카테고리 목록", example = "[\"DEV\", \"HEALTH\"]")
  private List<String> interestCategories;

  @Schema(description = "작성한 일지 수", example = "42")
  private Integer diaryCount;

  @Schema(description = "생성한 챌린지 수", example = "3")
  private Integer createdChallengeCount;

  @Schema(description = "참여한 챌린지 수", example = "8")
  private Integer participatedChallengeCount;

  @Builder
  public AdminMemberResponseDto(
      Long memberId,
      String nickname,
      String email,
      SignupRoute signupRoute,
      LocalDateTime createdAt,
      Job job,
      Gender gender,
      List<String> interestCategories,
      Integer diaryCount,
      Integer createdChallengeCount,
      Integer participatedChallengeCount) {
    this.memberId = memberId;
    this.nickname = nickname;
    this.email = email;
    this.signupRoute = signupRoute;
    this.createdAt = createdAt;
    this.job = job;
    this.gender = gender;
    this.interestCategories = interestCategories;
    this.diaryCount = diaryCount;
    this.createdChallengeCount = createdChallengeCount;
    this.participatedChallengeCount = participatedChallengeCount;
  }

  public static AdminMemberResponseDto fromMember(Member member) {
    List<String> interests =
        member.getMemberInterests().stream()
            .map(interest -> interest.getCategory().name())
            .toList();

    long diaryCount = member.getDiaries().stream().filter(d -> !d.getIsDeleted()).count();

    long createdChallengeCount =
        member.getChallenges().stream().filter(c -> c.getDeletedAt() == null).count();

    long participatedChallengeCount =
        member.getParticipants().stream()
            .filter(p -> p.getChallenge().getDeletedAt() == null)
            .count();

    return AdminMemberResponseDto.builder()
        .memberId(member.getId())
        .nickname(member.getNickname())
        .email(member.getEmail())
        .signupRoute(member.getSignupRoute())
        .createdAt(member.getCreatedAt())
        .job(member.getJob())
        .gender(member.getGender())
        .interestCategories(interests)
        .diaryCount((int) diaryCount)
        .createdChallengeCount((int) createdChallengeCount)
        .participatedChallengeCount((int) participatedChallengeCount)
        .build();
  }
}
