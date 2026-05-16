package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long> {

  @Query("select d.url from DiaryImage d where d.diary.id = :diaryId order by d.id limit 1")
  public String getDiaryThumbNail(@Param("diaryId") Long diaryId);
}
