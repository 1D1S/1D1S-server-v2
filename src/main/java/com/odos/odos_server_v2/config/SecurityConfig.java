package com.odos.odos_server_v2.config;

import com.odos.odos_server_v2.domain.security.jwt.JwtAuthenticationFilter;
import com.odos.odos_server_v2.domain.security.jwt.JwtTokenExceptionFilter;
import com.odos.odos_server_v2.domain.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.odos.odos_server_v2.domain.security.oauth2.handler.OAuth2LoginSuccessHandler;
import com.odos.odos_server_v2.domain.security.oauth2.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/", "/auth/**", "/oauth2/**", "/css/**", "/js/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth ->
                oauth
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new JwtTokenExceptionFilter(), JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.addExposedHeader("Authorization");
    config.addExposedHeader("Authorization-Refresh");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/oauth2/**", config);
    return source;
  }
}
