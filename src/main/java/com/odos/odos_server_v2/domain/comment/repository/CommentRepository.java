package com.odos.odos_server_v2.domain.comment.repository;

import com.odos.odos_server_v2.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  Page<Comment> findByDiaryIdAndParentIsNull(Long diaryId, Pageable pageable);

  Page<Comment> findByParentId(Long parentId, Pageable pageable);

  // List<Comment> findByParentId(Long parentId);

  long countByParentId(Long parentId);

  long countByDiaryId(Long diaryId);
}
