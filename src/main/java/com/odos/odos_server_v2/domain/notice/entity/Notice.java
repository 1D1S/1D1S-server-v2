package com.odos.odos_server_v2.domain.notice.entity;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.shared.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * 관리자 공지. pinned(고정) 공지는 목록 최상단에 노출된다.
 *
 * <p>author 는 작성 관리자 감사용으로만 저장하고 응답에는 노출하지 않는다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "notice",
    indexes = {@Index(name = "idx_notice_pinned_created", columnList = "is_pinned, created_at")})
public class Notice extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "is_pinned", nullable = false)
  private boolean pinned;

  /** 작성 관리자(감사용, 응답 미노출). */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_member_id")
  private Member author;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public static Notice create(String title, String content, boolean pinned, Member author) {
    return Notice.builder().title(title).content(content).pinned(pinned).author(author).build();
  }

  /** 부분 수정. null 인 인자는 기존 값 유지. */
  public void update(String title, String content, Boolean pinned) {
    if (title != null) {
      this.title = title;
    }
    if (content != null) {
      this.content = content;
    }
    if (pinned != null) {
      this.pinned = pinned;
    }
  }
}
