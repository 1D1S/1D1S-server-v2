package com.odos.odos_server_v2.domain.comment.repository;

import com.odos.odos_server_v2.domain.comment.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

  boolean existsByMemberIdAndCommentId(Long memberId, Long commentId);
}
