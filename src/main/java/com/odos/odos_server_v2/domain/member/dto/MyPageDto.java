package com.odos.odos_server_v2.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.diary.dto.DiaryResponse;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "마이페이지 응답")
@Builder
@Getter
public class MyPageDto {

  @Schema(description = "닉네임", example = "홍길동")
  String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  String profileUrl;

  @Schema(description = "로그인 유저의 이메일 주소", example = "1day1streak@naver.com")
  String email;

  // 본인 조회(getMyPage)에서만 채워지며, 타인 프로필 조회에는 절대 포함하지 않는다(개인정보).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "본인 휴대폰 번호(숫자만). 본인 마이페이지에서만 반환", example = "01012345678")
  String phoneNumber;

  @Schema(description = "회원가입 플랫폼", example = "NAVER")
  String provider;

  @Schema(description = "스트릭 통계 정보")
  StreakDto streak;

  @Schema(description = "참여 중인 챌린지 목록")
  List<ChallengeSummaryResponse> challengeList;

  @Schema(description = "내가 작성한 일지 목록")
  OffsetPagination<DiaryResponse> diaryList;

  @Schema(description = "관계 상태: FRIEND, REQUEST_SENT, REQUEST_RECEIVED, NONE, BLOCKED")
  String relationStatus;

  @Schema(description = "일지/스토리 접근 가능 여부")
  Boolean isAccessible;
}
