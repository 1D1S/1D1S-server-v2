package com.odos.odos_server_v2.domain.member;

import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

  public static Long getCurrentMemberIdOrNull() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    Object principal = auth.getPrincipal();

    if (principal instanceof MemberPrincipal mp) {
      return mp.getId();
    }
    // 일부 환경에선 문자열 "anonymousUser"를 principal로 씀
    if (principal instanceof String s && "anonymousUser".equals(s)) {
      return null;
    }
    // 예상치 못한 타입이면 게스트 취급
    return null;
  }
}
