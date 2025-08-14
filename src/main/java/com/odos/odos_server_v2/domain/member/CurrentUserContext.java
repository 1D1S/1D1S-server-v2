package com.odos.odos_server_v2.domain.member;

import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {
  private CurrentUserContext() {}

  public static MemberPrincipal getCurrentMemberPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
    if (!(auth.getPrincipal() instanceof MemberPrincipal principal)) {
      throw new CustomException(ErrorCode.INVALID_AUTH_PRINCIPAL);
    }
    return principal;
  }

  public static Long getCurrentMemberId() {
    return getCurrentMemberPrincipal().getId();
  }
}
