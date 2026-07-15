package com.odos.odos_server_v2.domain.security.oauth2.verification;

import com.odos.odos_server_v2.domain.member.entity.Enum.SignupRoute;

public record VerifiedSocialUser(SignupRoute provider, String subject, String email) {}
