package com.odos.odos_server_v2.domain.member.repository;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmailAndSignupRoute(String email, SignupRoute signupRoute);

  Optional<Member> findByRefreshToken(String refreshToken);

  @Query("SELECT m FROM Member m WHERE m.deletedAt IS NOT NULL AND m.deletedAt < :threshold")
  List<Member> findDeletableMembers(LocalDateTime threshold);

  boolean existsByNickname(String nickname);

  boolean existsByNicknameAndIdNot(String nickname, Long id);
}
