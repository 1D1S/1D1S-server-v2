package com.odos.odos_server_v2.domain.security.controller;

import static com.odos.odos_server_v2.response.ApiResponse.success;
import static com.odos.odos_server_v2.response.Message.TOKEN_REFRESH;

import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenProvider;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import com.odos.odos_server_v2.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTokenController {
  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;

  @GetMapping("/token")
  public ApiResponse<Void> reissueAccessToken(
      HttpServletRequest request, HttpServletResponse response) {

    String refreshToken =
        jwtTokenProvider
            .extractRefreshToken(request)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

    if (!jwtTokenProvider.isValidToken(refreshToken)) {
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }
    if (jwtTokenProvider.isExpired(refreshToken)) {
      throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    Member member =
        memberRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

    String newAccessToken = jwtTokenProvider.createAccessToken(member);
    String newRefreshToken = jwtTokenProvider.createRefreshToken();
    jwtTokenProvider.updateRefreshToken(member.getId(), newRefreshToken);
    jwtTokenProvider.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

    return success(TOKEN_REFRESH);
  }
}
