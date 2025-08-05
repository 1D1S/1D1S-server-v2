package com.odos.odos_server_v2.domain.security.oauth2.info;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.exception.CustomException;
import com.odos.odos_server_v2.exception.ErrorCode;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo get(SignupRoute signupRoute, Map<String, Object> attributes) {
        return switch (signupRoute) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            default -> throw new CustomException(ErrorCode.INVALID_SIGNUP_PROVIDER);
        };
    }
}
