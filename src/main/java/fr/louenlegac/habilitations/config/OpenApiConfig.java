package fr.louenlegac.habilitations.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI : /swagger-ui.html — bouton "Authorize" pour coller le JWT. */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API Habilitations", version = "0.1.0",
                description = "Gestion des demandes d'habilitations applicatives : workflow, RBAC, traçabilité"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
