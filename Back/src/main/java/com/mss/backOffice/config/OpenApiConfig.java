package com.mss.backOffice.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("BackOffice API")
                        .description("API documentation for MSS BackOffice services")
                        .version("v1")
                        .license(new License().name("Proprietary"))
                        .contact(new Contact().name("MSS Solutions").email("support@example.com")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Apply the bearer auth globally to all operations
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("backoffice")
                .packagesToScan("com.mss.backOffice.controller")
                // Only show specific endpoints - add the paths you want to expose
                .pathsToMatch(
                    "/BankSettlementController/*",
                    "/IntegrationFiles/*",
                    "/user/*",
                    "/downloadFile/*",
                    "/executorMobileThreads/*",
                        "/matchingUAP070IN/*",
                "/api/international-processing/*",
                    "/IntegrationFileInternational/*",
                    "/ChargeBacksController/*"
                    // Add more specific paths here as needed
                    // "/OtherController/specificEndpoint",
                    // "/AnotherController/anotherEndpoint"
                )
                .build();
    }
}
