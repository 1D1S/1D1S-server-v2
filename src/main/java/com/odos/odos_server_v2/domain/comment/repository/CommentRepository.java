package com.odos.odos_server_v2.domain.comment.repository;

import com.odos.odos_server_v2.domain.comment.entity.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  Page<Comment> findByDiaryIdAndParentIsNullAndIsDeletedFalse(Long diaryId, Pageable pageable);

  Page<Comment> findByParentIdAndIsDeletedFalse(Long parentId, Pageable pageable);

  List<Comment> findByParentId(Long parentId);

  long countByParentIdAndIsDeletedFalse(Long parentId);
}
