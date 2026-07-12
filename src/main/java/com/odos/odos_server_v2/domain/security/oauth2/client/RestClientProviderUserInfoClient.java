package com.odos.odos_server_v2.domain.security.oauth2.client;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * userinfo 엔드포인트를 provider 별 OAuth2 클라이언트 등록 정보(ClientRegistrationRepository)에서 그대로 가져와 호출한다.
 * kakao/naver 는 application-security.yml, google 은 Spring 기본 provider 정의에서 URI 를 얻는다. 반환된
 * attributes 는 웹 로그인과 동일한 형식이라 기존 OAuth2UserInfo 파서를 그대로 재사용할 수 있다.
 */
@Slf4j
@Component
public class RestClientProviderUserInfoClient implements ProviderUserInfoClient {

  private final ClientRegistrationRepository clientRegistrationRepository;
  private final RestClient restClient;

  public RestClientProviderUserInfoClient(
      ClientRegistrationRepository clientRegistrationRepository) {
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.restClient = RestClient.create();
  }

  @Override
  public Map<String, Object> fetchAttributes(SignupRoute signupRoute, String providerAccessToken) {
    String userInfoUri = resolveUserInfoUri(signupRoute);

    try {
      Map<String, Object> attributes =
          restClient
              .get()
              .uri(userInfoUri)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + providerAccessToken)
              .retrieve()
              .body(new ParameterizedTypeReference<Map<String, Object>>() {});

      if (attributes == null || attributes.isEmpty()) {
        throw new CustomException(ErrorCode.OAUTH_PROVIDER_TOKEN_INVALID);
      }
      return attributes;
    } catch (RestClientException e) {
      log.warn("소셜 provider userinfo 조회 실패: provider={}", signupRoute, e);
      throw new CustomException(ErrorCode.OAUTH_PROVIDER_TOKEN_INVALID);
    }
  }

  private String resolveUserInfoUri(SignupRoute signupRoute) {
    ClientRegistration registration =
        clientRegistrationRepository.findByRegistrationId(signupRoute.name().toLowerCase());
    if (registration == null) {
      throw new CustomException(ErrorCode.INVALID_SIGNUP_PROVIDER);
    }

    String userInfoUri = registration.getProviderDetails().getUserInfoEndpoint().getUri();
    if (userInfoUri == null || userInfoUri.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_SIGNUP_PROVIDER);
    }
    return userInfoUri;
  }
}
