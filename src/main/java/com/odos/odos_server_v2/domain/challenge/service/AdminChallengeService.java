package com.odos.odos_server_v2.domain.challenge.service;

import com.odos.odos_server_v2.domain.challenge.dto.ChallengeDeleteAdminRequest;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeDeleteAdminResponse;
import com.odos.odos_server_v2.domain.challenge.dto.ChallengeSummaryResponse;
import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import com.odos.odos_server_v2.domain.challenge.repository.ChallengeRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import com.odos.odos_server_v2.domain.shared.dto.OffsetPagination;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminChallengeService {

  private final ChallengeRepository challengeRepository;
  private final MemberRepository memberRepository;
  private final ChallengeService challengeService;

  private Member requireAdmin() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (!member.getRole().equals(MemberRole.ADMIN)) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    return member;
  }

  @Transactional
  public OffsetPagination<ChallengeSummaryResponse> getChallenges(
      String status, Category category, String authorNickname, String sortBy, Pageable pageable) {
    Member admin = requireAdmin();

    String normalizedStatus = normalizeStatus(status);
    String normalizedNickname =
        authorNickname == null || authorNickname.isBlank() ? null : authorNickname.trim();
    String normalizedSortBy =
        sortBy == null || sortBy.isBlank() ? "latest" : sortBy.trim().toLowerCase();

    if (!normalizedSortBy.equals("latest") && !normalizedSortBy.equals("likes")) {
      throw new CustomException(ErrorCode.INVALID_CHALLENGE_ADMIN_FILTER);
    }

    LocalDate today = LocalDate.now();
    Page<Challenge> challenges =
        normalizedSortBy.equals("likes")
            ? challengeRepository.findAdminChallengesOrderByLikes(
                normalizedStatus, category, normalizedNickname, today, pageable)
            : challengeRepository.findAdminChallengesOrderByLatest(
                normalizedStatus, category, normalizedNickname, today, pageable);

    Page<ChallengeSummaryResponse> result =
        challenges.map(c -> challengeService.toChallengeSummary(c, admin.getId()));
    return OffsetPagination.from(result);
  }

  @Transactional
  public ChallengeDeleteAdminResponse deleteChallenge(
      Long challengeId, ChallengeDeleteAdminRequest request) {
    requireAdmin();
    if (request == null
        || request.getDeleteReason() == null
        || request.getDeleteReason().isBlank()) {
      throw new CustomException(ErrorCode.CHALLENGE_DELETED_REASON_NULL);
    }

    Challenge challenge =
        challengeRepository
            .findById(challengeId)
            .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

    challenge.updateDeletedReason(request.getDeleteReason());
    challenge.softDelete();
    Challenge deleted = challengeRepository.save(challenge);
    return ChallengeDeleteAdminResponse.from(deleted);
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String upper = status.trim().toUpperCase();
    if (!upper.equals("ONGOING") && !upper.equals("UPCOMING") && !upper.equals("ENDED")) {
      throw new CustomException(ErrorCode.INVALID_CHALLENGE_ADMIN_FILTER);
    }
    return upper;
  }
}
