package com.anhvt.epms.procurement.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Swagger UI
 * Configures API documentation and JWT Bearer Authentication support
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Enterprise Procurement Management System API",
        version = "1.0",
        description = "API documentation for Procurement System"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter JWT token to authorize"
)
public class OpenApiConfig {
    // Configuration handled by annotations
}
