package com.odos.odos_server_v2.domain.security.oauth2.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class CustomOAuth2AuthorizationRequestResolver
    implements OAuth2AuthorizationRequestResolver {
  private final OAuth2AuthorizationRequestResolver defaultResolver;
  private final String authorizationRequestBaseUri;

  public CustomOAuth2AuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository,
      String authorizationRequestBaseUri) {
    this.defaultResolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, authorizationRequestBaseUri);
    this.authorizationRequestBaseUri = authorizationRequestBaseUri;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    String registrationId = resolveRegistrationId(request);
    if (registrationId == null) {
      return null;
    }
    return resolve(request, registrationId);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
    return customize(defaultResolver.resolve(request, registrationId), request, registrationId);
  }

  private OAuth2AuthorizationRequest customize(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      String registrationId) {
    if (authorizationRequest == null) {
      return null;
    }

    String frontendOrigin =
        extractFrontendOrigin(request).orElseGet(() -> buildRequestOrigin(request));
    String frontendHost = extractHost(frontendOrigin);
    if (frontendHost == null || frontendHost.isBlank()) {
      return authorizationRequest;
    }

    String redirectUri = frontendOrigin + "/login/oauth2/code/" + registrationId;
    return OAuth2AuthorizationRequest.from(authorizationRequest).redirectUri(redirectUri).build();
  }

  private Optional<String> extractFrontendOrigin(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader("Origin"))
        .filter(origin -> !origin.isBlank())
        .or(
            () ->
                Optional.ofNullable(request.getHeader("Referer"))
                    .flatMap(this::extractOriginFromReferer));
  }

  private String resolveRegistrationId(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    String prefix = contextPath + authorizationRequestBaseUri + "/";
    if (!uri.startsWith(prefix)) {
      return null;
    }
    String registrationId = uri.substring(prefix.length());
    int slashIndex = registrationId.indexOf('/');
    if (slashIndex != -1) {
      registrationId = registrationId.substring(0, slashIndex);
    }
    return registrationId.isBlank() ? null : registrationId;
  }

  private Optional<String> extractOriginFromReferer(String referer) {
    try {
      URI uri = new URI(referer);
      String scheme = uri.getScheme();
      String host = uri.getHost();
      int port = uri.getPort();
      if (scheme == null || host == null) {
        return Optional.empty();
      }
      if (port == -1) {
        return Optional.of(scheme + "://" + host);
      }
      return Optional.of(scheme + "://" + host + ":" + port);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private String buildRequestOrigin(HttpServletRequest request) {
    String scheme =
        Optional.ofNullable(request.getHeader("X-Forwarded-Proto"))
            .filter(value -> !value.isBlank())
            .orElse(request.getScheme());
    String host =
        Optional.ofNullable(request.getHeader("X-Forwarded-Host"))
            .filter(value -> !value.isBlank())
            .orElseGet(
                () ->
                    Optional.ofNullable(request.getHeader("Host"))
                        .filter(value -> !value.isBlank())
                        .orElse(request.getServerName()));
    return scheme + "://" + host;
  }

  private String extractHost(String origin) {
    try {
      return new URI(origin).getHost();
    } catch (Exception e) {
      return null;
    }
  }
}
