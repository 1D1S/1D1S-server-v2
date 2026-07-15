package com.odos.odos_server_v2.domain.security.oauth2.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.member.service.MemberDeleteService;
import com.odos.odos_server_v2.domain.security.dto.NativeSessionCodeResponse;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.service.NativeSessionService;
import com.odos.odos_server_v2.domain.security.service.SessionTokenIssuer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private MemberRepository memberRepository;
  @Mock private MemberDeleteService memberDeleteService;
  @Mock private SessionTokenIssuer sessionTokenIssuer;
  @Mock private NativeSessionService nativeSessionService;
  @Mock private Authentication authentication;

  @Test
  void nativeBrowserLoginReturnsPkceBoundCodeWithoutCreatingWebSession() throws Exception {
    String challenge = "0123456789012345678901234567890123456789012";
    Member member =
        Member.builder().id(1L).email("member@example.com").signupRoute(SignupRoute.NAVER).build();
    MemberPrincipal principal =
        new MemberPrincipal(1L, "member@example.com", "MEMBER", SignupRoute.NAVER);
    when(authentication.getPrincipal()).thenReturn(principal);
    when(memberRepository.findByEmailAndSignupRoute("member@example.com", SignupRoute.NAVER))
        .thenReturn(Optional.of(member));
    when(nativeSessionService.issueCode(1L, challenge))
        .thenReturn(new NativeSessionCodeResponse("login-code", 60));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("nativeCodeChallenge", challenge);
    MockHttpServletResponse response = new MockHttpServletResponse();
    OAuth2LoginSuccessHandler handler =
        new OAuth2LoginSuccessHandler(
            jwtTokenProvider,
            new ObjectMapper(),
            memberRepository,
            memberDeleteService,
            sessionTokenIssuer,
            nativeSessionService);

    handler.onAuthenticationSuccess(request, response, authentication);

    assertTrue(response.getContentAsString().contains("login-code"));
    verify(sessionTokenIssuer, never()).issue(any(), any());
    verify(jwtTokenProvider, never()).sendAccessAndRefreshToken(any(), any(), any());
  }
}
