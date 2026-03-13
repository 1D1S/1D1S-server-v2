package com.odos.odos_server_v2.domain.member.repository;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByEmailAndSignupRoute(String email, SignupRoute signupRoute);

  Optional<Member> findByRefreshToken(String refreshToken);
}
