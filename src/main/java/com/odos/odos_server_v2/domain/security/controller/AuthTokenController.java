package com.odos.odos_server_v2.domain.security.controller;

import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.oauth2.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTokenController {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @GetMapping("/token")
    @ResponseBody
    public ResponseEntity<TokenResponse> reissueAccessToken(
            @RequestHeader("Authorization-Refresh") String refreshHeader) {

        String refreshToken =
                jwtTokenProvider
                        .extractTokenFromHeader(refreshHeader)
                        .orElseThrow(() -> new JwtException("RefreshToken is missing or invalid format"));

        if (!jwtTokenProvider.isValidToken(refreshToken) || jwtTokenProvider.isExpired(refreshToken)) {
            throw new JwtException("RefreshToken is invalid or expired");
        }

        return memberRepository
                .findByRefreshToken(refreshToken)
                .map(
                        member -> {
                            String newAccessToken = jwtTokenProvider.createAccessToken(member);
                            String newRefreshToken = jwtTokenProvider.createRefreshToken();
                            jwtTokenProvider.updateRefreshToken(member.getEmail(), newRefreshToken);

                            TokenResponse response =
                                    TokenResponse.builder()
                                            .accessToken(newAccessToken)
                                            .refreshToken(newRefreshToken)
                                            .build();

                            return ResponseEntity.ok(response);
                        })
                .orElseThrow(() -> new JwtException("RefreshToken does not match any user"));
    }
}
