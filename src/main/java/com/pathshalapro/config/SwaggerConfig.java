package com.pathshalapro.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI documentation configuration.
 * Access at: <base-url>/api/v1/swagger-ui.html
 *
 * The server URL is driven by the {@code app.server-url} property:
 *   - Dev:  defaults to http://localhost:8080  (no extra config needed)
 *   - Prod: set APP_SERVER_URL=https://pathshala-pro-backend.onrender.com
 *           (or your custom domain) in the Render environment variables.
 */
@Configuration
public class SwaggerConfig {

    /** Injected from app.server-url — resolves differently per profile. */
    @Value("${app.server-url}")
    private String serverUrl;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url(serverUrl + "/api/v1")
                                .description("API Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide JWT token obtained from /auth/login endpoint")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("PathshalaPro API")
                .description("SaaS School Management System REST API Documentation. " +
                        "Multi-tenant platform for managing schools, students, teachers, fees, and more.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("PathshalaPro Team")
                        .email("support@pathshalapro.com")
                        .url("https://pathshalapro.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://pathshalapro.com/license"));
    }
}
