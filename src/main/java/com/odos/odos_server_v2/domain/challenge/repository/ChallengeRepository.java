package com.odos.odos_server_v2.domain.challenge.repository;

import com.odos.odos_server_v2.domain.challenge.entity.Challenge;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
  @Query(
      """
    select c
      from Challenge c
     where (:cursorId is null or c.id < :cursorId)
       and ( :keyword = ''
             or lower(c.title) like concat('%', lower(:keyword), '%')
             or lower(c.description) like concat('%', lower(:keyword), '%') )
     order by c.id desc
  """)
  List<Challenge> searchPage(
      @Param("cursorId") Long cursorId, @Param("keyword") String keyword, Pageable pageable);
}
