package com.odos.odos_server_v2.domain.admin.dto;

import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 회원 조회 필터 요청")
public class AdminMemberFilterRequest {

  @Schema(description = "성별 필터 (MALE, FEMALE, ETC)", example = "MALE")
  private Gender gender;

  @Schema(description = "직업 상태 필터 (STUDENT, WORKER)", example = "STUDENT")
  private Job job;

  @Schema(description = "가입한 SNS 필터 (KAKAO, NAVER, GOOGLE, APPLE)", example = "KAKAO")
  private SignupRoute signupRoute;

  public AdminMemberFilterRequest(Gender gender, Job job, SignupRoute signupRoute) {
    this.gender = gender;
    this.job = job;
    this.signupRoute = signupRoute;
  }
}
