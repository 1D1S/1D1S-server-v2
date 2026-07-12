package com.odos.odos_server_v2.domain.popup.entity;

import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 홈 팝업(어드민 설정). 게시 기간(startDate~endDate) 안이고 isActive=true 면 오늘(KST) 게시 대상.
 *
 * <p>popupKey 는 클라 "다시보지않기" 쿠키 키로 노출되는 안정적 식별자(UUID). DB id 대신 사용해 내부 식별자 노출을 피한다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "popup",
    indexes = {
      @Index(name = "idx_popup_active_dates", columnList = "is_active, start_date, end_date")
    })
public class Popup extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "popup_key", nullable = false, unique = true, length = 36)
  private String popupKey;

  @Column(name = "image_url", nullable = false, length = 512)
  private String imageUrl;

  @Column(name = "cta_text", nullable = false)
  private String ctaText;

  @Column(name = "link_url", nullable = false, length = 512)
  private String linkUrl;

  /** 어드민 관리용 이름(선택). 클라 노출 X. */
  @Column private String title;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  public static Popup create(
      String imageUrl,
      String ctaText,
      String linkUrl,
      String title,
      LocalDate startDate,
      LocalDate endDate) {
    return Popup.builder()
        .popupKey(UUID.randomUUID().toString())
        .imageUrl(imageUrl)
        .ctaText(ctaText)
        .linkUrl(linkUrl)
        .title(title)
        .startDate(startDate)
        .endDate(endDate)
        .isActive(true)
        .build();
  }

  /** 부분 수정. null 인 인자는 기존 값 유지. */
  public void update(
      String imageUrl,
      String ctaText,
      String linkUrl,
      String title,
      LocalDate startDate,
      LocalDate endDate,
      Boolean isActive) {
    if (imageUrl != null) {
      this.imageUrl = imageUrl;
    }
    if (ctaText != null) {
      this.ctaText = ctaText;
    }
    if (linkUrl != null) {
      this.linkUrl = linkUrl;
    }
    if (title != null) {
      this.title = title;
    }
    if (startDate != null) {
      this.startDate = startDate;
    }
    if (endDate != null) {
      this.endDate = endDate;
    }
    if (isActive != null) {
      this.isActive = isActive;
    }
  }
}
