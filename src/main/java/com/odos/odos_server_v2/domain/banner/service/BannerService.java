package com.odos.odos_server_v2.domain.banner.service;

import com.odos.odos_server_v2.domain.banner.dto.BannerCreateRequest;
import com.odos.odos_server_v2.domain.banner.dto.BannerResponse;
import com.odos.odos_server_v2.domain.banner.entity.Banner;
import com.odos.odos_server_v2.domain.banner.repository.BannerRepository;
import com.odos.odos_server_v2.domain.member.CurrentUserContext;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final BannerRepository bannerRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public BannerResponse create(BannerCreateRequest request) {
    requireAdmin();
    validateRequired(request);
    validatePeriod(request.getStartDate(), request.getEndDate());

    Banner saved = bannerRepository.save(toEntity(request));
    return BannerResponse.from(saved);
  }

  public List<BannerResponse> getTodayBanners() {
    requireAdmin();
    return bannerRepository.findTodayBanners(LocalDate.now(KST)).stream()
        .map(BannerResponse::from)
        .toList();
  }

  public List<BannerResponse> getAllBanners() {
    requireAdmin();
    return bannerRepository.findAllByOrderByStartDateAscIdAsc().stream()
        .map(BannerResponse::from)
        .toList();
  }

  private Banner toEntity(BannerCreateRequest request) {
    return Banner.builder()
        .title(request.getTitle())
        .subtitle(request.getSubtitle())
        .imageUrl(request.getImageUrl())
        .linkUrl(request.getLinkUrl())
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .build();
  }

  private void requireAdmin() {
    Long memberId = CurrentUserContext.getCurrentMemberId();
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    if (member.getRole() != MemberRole.ADMIN) {
      throw new CustomException(ErrorCode.MEMBER_NOT_ADMIN);
    }
  }

  private void validateRequired(BannerCreateRequest request) {
    if (request == null
        || isBlank(request.getTitle())
        || isBlank(request.getSubtitle())
        || isBlank(request.getImageUrl())
        || isBlank(request.getLinkUrl())
        || request.getStartDate() == null
        || request.getEndDate() == null) {
      throw new CustomException(ErrorCode.BANNER_REQUIRED_FIELD_MISSING);
    }
  }

  private void validatePeriod(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
      throw new CustomException(ErrorCode.INVALID_BANNER_PERIOD);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
