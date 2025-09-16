package com.odos.odos_server_v2.domain.member.repository;

import com.odos.odos_server_v2.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmail(String email);

  Optional<Member> findByRefreshToken(String refreshToken);
}
