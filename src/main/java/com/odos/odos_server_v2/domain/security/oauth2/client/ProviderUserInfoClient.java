package com.odos.odos_server_v2.domain.security.oauth2.client;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import java.util.Map;

/** provider access_token 으로 소셜 provider 의 userinfo 를 조회한다. */
public interface ProviderUserInfoClient {
  Map<String, Object> fetchAttributes(SignupRoute signupRoute, String providerAccessToken);
}
