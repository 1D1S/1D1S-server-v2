package com.odos.odos_server_v2.domain.security.jwt;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.security.entity.SessionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class MemberPrincipal implements UserDetails, OAuth2User {
  private final Long id;
  private final String email;
  private final String role;
  private final SignupRoute signupRoute;
  private final SessionType sessionType;
  private final String sessionId;

  public MemberPrincipal(Long id, String email, String role, SignupRoute signupRoute) {
    this(id, email, role, signupRoute, SessionType.WEBVIEW, null);
  }

  public MemberPrincipal(
      Long id,
      String email,
      String role,
      SignupRoute signupRoute,
      SessionType sessionType,
      String sessionId) {
    this.id = id;
    this.email = email;
    this.role = role;
    this.signupRoute = signupRoute;
    this.sessionType = sessionType;
    this.sessionId = sessionId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(() -> "ROLE_" + role);
  }

  @Override
  public String getPassword() {
    return "";
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public String getName() {
    return String.valueOf(id);
  }
}
