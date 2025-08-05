package com.odos.odos_server_v2.domain.security.oauth2.service;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;
import com.odos.odos_server_v2.domain.member.entity.Member;
import com.odos.odos_server_v2.domain.member.entity.Enum.MemberRole;
import com.odos.odos_server_v2.domain.member.repository.MemberRepository;
import com.odos.odos_server_v2.domain.security.jwt.MemberPrincipal;
import com.odos.odos_server_v2.domain.security.oauth2.info.OAuth2UserInfo;
import com.odos.odos_server_v2.domain.security.oauth2.info.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        SignupRoute signupRoute =
                SignupRoute.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.get(signupRoute, attributes);
        String email = userInfo.getEmail();

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_email"), "This is an invalid_email.");
        }

        Member member =
                memberRepository
                        .findByEmail(email)
                        .orElseGet(() -> createMember(email, signupRoute, userInfo.getId()));

        log.debug(">>> OAuth2 attributes: {}", attributes);
        log.info(
                "OAuth2UserService provider = {}", userRequest.getClientRegistration().getRegistrationId());

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
        log.info(
                "new member saved : id={}, email={}, provider={}",
                saved.getId(),
                saved.getEmail(),
                saved.getSignupRoute());
        return saved;
    }
}
