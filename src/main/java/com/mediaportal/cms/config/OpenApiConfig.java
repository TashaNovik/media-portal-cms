package com.mediaportal.cms.config;

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
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Media Portal CMS}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version("1.0.0")
                        .description("""
                                REST API for Media Portal Content Management System.
                                
                                ## Features
                                - Article, Video, Podcast management with full CRUD operations
                                - JWT-based authentication and authorization
                                - Real-time analytics using Redis (INCR, ZSET, TTL)
                                - Content recommendations based on view statistics
                                - Caching with @Cacheable annotation
                                
                                ## Authentication
                                Use the /api/auth/login endpoint to obtain a JWT token, 
                                then include it in the Authorization header as: Bearer {token}
                                
                                ## Redis Features Demonstrated
                                - **INCR**: Atomic view counter increments
                                - **ZSET**: Sorted sets for content rankings
                                - **SET**: Unique visitor tracking
                                - **TTL**: Automatic data expiration
                                - **@Cacheable**: Spring Cache integration
                                """)
                        .contact(new Contact()
                                .name("МФТИ Student")
                                .email("student@phystech.edu")
                                .url("https://mipt.ru"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login")));
    }
}
