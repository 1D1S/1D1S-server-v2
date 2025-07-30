package com.odos.odos_server_v2.domain.diary.repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {}
