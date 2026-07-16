package com.odos.odos_server_v2.domain.notice.dto;

import com.odos.odos_server_v2.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** 공지 응답(관리자/사용자 공용). 작성자 정보는 노출하지 않는다. */
@Schema(description = "공지 응답")
public record NoticeResponse(
    Long id,
    String title,
    String content,
    boolean pinned,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static NoticeResponse from(Notice n) {
    return new NoticeResponse(
        n.getId(), n.getTitle(), n.getContent(), n.isPinned(), n.getCreatedAt(), n.getUpdatedAt());
  }
}
