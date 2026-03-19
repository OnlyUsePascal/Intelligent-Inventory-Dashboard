package com.keyloop.inventory.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 * Adds global header parameters for tenant and employee context.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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

    /**
     * Add tenant/employee headers to all API operations.
     */
    @Bean
    public OperationCustomizer globalHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            // Skip adding headers to actuator and swagger endpoints
            String path = handlerMethod.getMethod().getDeclaringClass().getName();
            if (path.contains("actuator") || path.contains("swagger")) {
                return operation;
            }

            Parameter tenantIdHeader = new Parameter()
                    .in("header")
                    .name("X-Tenant-Id")
                    .description("Tenant (dealership) UUID")
                    .required(true)
                    .schema(new io.swagger.v3.oas.models.media.Schema<String>()
                            .type("string")
                            .format("uuid")
                            .example("550e8400-e29b-41d4-a716-446655440000"));

            Parameter employeeIdHeader = new Parameter()
                    .in("header")
                    .name("X-Employee-Id")
                    .description("Employee UUID")
                    .required(true)
                    .schema(new io.swagger.v3.oas.models.media.Schema<String>()
                            .type("string")
                            .format("uuid")
                            .example("550e8400-e29b-41d4-a716-446655440001"));

            Parameter employeeRoleHeader = new Parameter()
                    .in("header")
                    .name("X-Employee-Role")
                    .description("Employee role: ADMIN, INVENTORY, or SALE")
                    .required(true)
                    .schema(new io.swagger.v3.oas.models.media.Schema<String>()
                            .type("string")
                            ._enum(List.of("ADMIN", "INVENTORY", "SALE"))
                            .example("ADMIN"));

            operation.addParametersItem(tenantIdHeader);
            operation.addParametersItem(employeeIdHeader);
            operation.addParametersItem(employeeRoleHeader);

            return operation;
        };
    }
}
