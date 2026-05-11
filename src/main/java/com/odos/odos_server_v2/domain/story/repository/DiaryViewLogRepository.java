package com.odos.odos_server_v2.domain.story.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.odos.odos_server_v2.domain.diary.entity.Diary;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.story.entity.DiaryViewLog;

@Repository
public interface DiaryViewLogRepository extends JpaRepository<DiaryViewLog, Long> {

  // 특정 회원이 특정 일지를 시청했는지 확인
  boolean existsByMemberAndDiary(Member member, Diary diary);

  // 특정 회원이 시청한 일지 목록
  List<DiaryViewLog> findByMember(Member member);

  // 특정 회원이 시청한 특정 친구의 모든 일지
  List<DiaryViewLog> findByMemberAndDiaryIn(Member member, List<Diary> diaries);

  // 특정 일지를 시청한 회원 조회
  Optional<DiaryViewLog> findByDiary(Diary diary);
}
