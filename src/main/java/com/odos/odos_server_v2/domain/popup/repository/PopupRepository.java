package com.odos.odos_server_v2.domain.popup.repository;

import com.odos.odos_server_v2.domain.popup.entity.Popup;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

  /** 오늘(:today) 게시 중인 활성 팝업. 시작일 오름차순(같으면 id 오름차순). */
  @Query(
      """
      select p from Popup p
      where p.isActive = true
        and p.startDate <= :today
        and p.endDate >= :today
      order by p.startDate asc, p.id asc
      """)
  List<Popup> findActiveOn(@Param("today") LocalDate today);

  /**
   * 어드민 달력 뷰: 기간 [from, to] 와 게시기간이 겹치는 팝업. from/to 는 선택(null 이면 해당 경계 무시 → 전체).
   *
   * <p>겹침 조건: startDate <= to AND endDate >= from. optional 파라미터는 Postgres 타입 추론을 위해 cast 로 타입을
   * 명시한다.
   */
  @Query(
      """
      select p from Popup p
      where (cast(:from as date) is null or p.endDate >= :from)
        and (cast(:to as date) is null or p.startDate <= :to)
      order by p.startDate asc, p.id asc
      """)
  List<Popup> findForAdminCalendar(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
