package com.odos.odos_server_v2.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    String jwt = "JWT";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
    Components components =
        new Components()
            .addSecuritySchemes(
                jwt,
                new SecurityScheme()
                    .name(jwt)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
    return new OpenAPI()
        .components(new Components())
        .info(apiInfo())
        .addSecurityItem(securityRequirement)
        .components(components);
  }

  private Info apiInfo() {
    return new Info().title("1D1S server-v2").description("1d1s 백엔드 api 테스트 입니다").version("2.0.0");
  }
}
