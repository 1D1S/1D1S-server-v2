package com.odos.odos_server_v2.domain.admin.repository;

import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberStatus;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminMemberRepository extends JpaRepository<Member, Long> {

  /**
   * 필터링 및 페이지네이션과 함께 회원 조회
   *
   * @param gender 성별 필터 (null이면 모든 성별)
   * @param job 직업 상태 필터 (null이면 모든 직업 상태)
   * @param signupRoute 가입 경로 필터 (null이면 모든 가입 경로)
   * @param status 회원 상태 필터
   * @param pageable 페이지네이션 정보
   * @return 필터링된 회원 페이지
   */
  @Query(
      "SELECT m FROM Member m "
          + "WHERE m.status = :status "
          + "AND (:gender IS NULL OR m.gender = :gender) "
          + "AND (:job IS NULL OR m.job = :job) "
          + "AND (:signupRoute IS NULL OR m.signupRoute = :signupRoute) "
          + "ORDER BY m.createdAt DESC")
  Page<Member> findMembersWithFilters(
      @Param("gender") Gender gender,
      @Param("job") Job job,
      @Param("signupRoute") SignupRoute signupRoute,
      @Param("status") MemberStatus status,
      Pageable pageable);
}
