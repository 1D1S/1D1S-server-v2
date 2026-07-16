package com.odos.odos_server_v2.domain.banner.repository;

import com.odos.odos_server_v2.domain.banner.entity.Banner;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

  List<Banner> findAllByOrderByStartDateAscIdAsc();

  @Query(
      """
      select b from Banner b
      where b.startDate <= :today
        and b.endDate >= :today
      order by b.startDate asc, b.id asc
      """)
  List<Banner> findTodayBanners(@Param("today") LocalDate today);
}
