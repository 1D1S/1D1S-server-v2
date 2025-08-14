package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.TOKEN_REFRESH;

import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.domain.security.oauth2.TokenResponse;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import com.odos.odos_server_v2.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTokenController {
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;

  @GetMapping("/token")
  @ResponseBody
  public ApiResponse<TokenResponse> reissueAccessToken(
      @RequestHeader("Authorization-Refresh") String refreshHeader) {

    String refreshToken =
        jwtTokenProvider
            .extractTokenFromHeader(refreshHeader)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

    if (!jwtTokenProvider.isValidToken(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    if (jwtTokenProvider.isExpired(refreshToken)) {
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
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

              return success(TOKEN_REFRESH, response);
            })
        .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
  }
}
