package com.writingboard.server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server prodServer = new Server();
        prodServer.setUrl("https://hsh-server.com");
        prodServer.setDescription("운영 서버 (HTTPS)");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 서버 (HTTP)");

        return new OpenAPI()
                .servers(List.of(prodServer, localServer)) // 서버 리스트 추가
                .info(new Info()
                        .title("Writing Board Server API")
                        .version("1.0")
                        .description("회의실 및 화이트보드 서비스 API"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요 (Bearer 제외)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}