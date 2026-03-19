package com.keyloop.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration.
 * Adds shared user context security scheme.
 */
@Configuration
public class OpenApiConfig {

    private static final String USER_CONTEXT_SCHEME = "UserContextHeader";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(USER_CONTEXT_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Context")
                                .description("X-User-Context header in format tenant|employee|role. Example: tenant|employee|role")))
                .addSecurityItem(new SecurityRequirement().addList(USER_CONTEXT_SCHEME))
                .info(new Info()
                        .title("Intelligent Inventory Dashboard API")
                        .version("1.0.0")
                        .description("Backend RESTful service for Keyloop's Supply product domain. " +
                                "Provides dealerships with real-time, centralized visibility over their vehicle inventory pipeline.")
                        .contact(new Contact()
                                .name("Keyloop")
                                .url("https://www.keyloop.com"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
