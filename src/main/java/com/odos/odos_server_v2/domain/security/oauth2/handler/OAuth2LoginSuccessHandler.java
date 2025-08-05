package com.odos.odos_server_v2.domain.security.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.oauth2.OAuth2LoginResponse;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        String email = principal.getEmail();

        Member member =
                memberRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.createAccessToken(member);

        String existingRefresh = member.getRefreshToken();
        boolean needNewRefresh =
                (existingRefresh == null
                        || !jwtTokenProvider.isValidToken(existingRefresh)
                        || jwtTokenProvider.isExpired(existingRefresh));

        String refreshToken = needNewRefresh ? jwtTokenProvider.createRefreshToken() : existingRefresh;

        if (needNewRefresh) {
            jwtTokenProvider.updateRefreshToken(email, refreshToken);
        }

        boolean isProfileComplete =
                member.getNickname() != null
                        // && member.getMemberProfileImageUrl() != null
                        && member.getJob() != null
                        && member.getBirth() != null
                        && member.getGender() != null
                        && member.getIsPublic() != null;

        OAuth2LoginResponse dto =
                OAuth2LoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .isProfileComplete(isProfileComplete)
                        .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(dto));

        log.debug("OAuth2 login success - {} (profileComplete={})", email, isProfileComplete);
    }
}
