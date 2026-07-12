package com.odos.odos_server_v2.domain.security.oauth2.service;

import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.oauth2.info.OAuth2UserInfo;
import com.odos.odos_server_v2.domain.security.oauth2.info.OAuth2UserInfoFactory;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
  private final MemberRepository memberRepository;

  // 소셜 provider userinfo 조회용 RestTemplate. 타임아웃 미설정 시 느린 provider 가 로그인 스레드를
  // 무한정 잡는다. DefaultOAuth2UserService 기본과 동일한 에러 핸들러 + connect/read 타임아웃.
  private static DefaultOAuth2UserService timeoutBoundedUserService() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
    factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(factory);
    restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
    DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    delegate.setRestOperations(restTemplate);
    return delegate;
  }

  @Transactional
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    DefaultOAuth2UserService delegate = timeoutBoundedUserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    SignupRoute signupRoute =
        SignupRoute.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
    Map<String, Object> attributes = oAuth2User.getAttributes();

    OAuth2UserInfo userInfo = OAuth2UserInfoFactory.get(signupRoute, attributes);
    String email = userInfo.getEmail();

    if (email == null || email.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_OAUTH_EMAIL);
    }

    Member member =
        memberRepository
            .findByEmailAndSignupRoute(email, signupRoute)
            .orElseGet(() -> createMember(email, signupRoute, userInfo.getId()));

    return new MemberPrincipal(
        member.getId(), member.getEmail(), member.getRole().name(), member.getSignupRoute());
  }

  private Member createMember(String email, SignupRoute signupRoute, String socialId) {
    Member newMember =
        Member.builder()
            .email(email)
            .signupRoute(signupRoute)
            .socialId(socialId)
            .role(MemberRole.GUEST)
            .build();

    Member saved = memberRepository.save(newMember);
    return saved;
  }
}
