package com.odos.odos_server_v2.domain.notice.repository;

import com.odos.odos_server_v2.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

  /**
   * 목록 정렬: 고정 먼저 → 고정끼리는 등록순(id ASC, 먼저 등록된 게 위) → 비고정은 최신순(createdAt DESC).
   *
   * <p>Pageable 에는 정렬을 넣지 말 것(여기 ORDER BY 와 충돌). page/size 만 전달한다.
   */
  @Query(
      value =
          "SELECT n FROM Notice n ORDER BY n.pinned DESC, "
              + "CASE WHEN n.pinned = true THEN n.id END ASC, "
              + "n.createdAt DESC, n.id DESC",
      countQuery = "SELECT COUNT(n) FROM Notice n")
  Page<Notice> findAllOrdered(Pageable pageable);
}
