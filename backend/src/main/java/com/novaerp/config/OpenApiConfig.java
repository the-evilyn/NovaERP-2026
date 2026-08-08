package com.novaerp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger Configuration for NovaERP REST APIs.
 * Configures JWT Bearer authentication scheme, API metadata, servers, and module tags.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NovaERP - Intelligent ERP Platform API")
                        .version("1.0.0")
                        .description("Production-grade RESTful API documentation for NovaERP: Commercial Management, " +
                                "Inventory Tracking, Automated Invoicing, Payments, Audit Trails, and AI Decision Support.")
                        .contact(new Contact()
                                .name("NovaERP Architecture Team")
                                .email("engineering@novaerp.com")
                                .url("https://novaerp.com"))
                        .license(new License()
                                .name("Proprietary - Enterprise License")
                                .url("https://novaerp.com/license")))
                .servers(List.of(
                        new Server().url(contextPath).description("Current Environment API Gateway"),
                        new Server().url("http://localhost:8080/api/v1").description("Local Development Server"),
                        new Server().url("https://api.novaerp.internal/api/v1").description("Production Environment")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token to authorize requests. Format: Bearer {token}")))
                .tags(List.of(
                        new Tag().name("Authentication").description("User authentication, JWT login, registration, and token refresh"),
                        new Tag().name("Users").description("User profile and account administration"),
                        new Tag().name("Roles").description("Role-Based Access Control (RBAC) definitions"),
                        new Tag().name("Clients").description("Commercial client and customer management"),
                        new Tag().name("Suppliers").description("Supplier and vendor relationship management"),
                        new Tag().name("Categories").description("Product category hierarchy management"),
                        new Tag().name("Products").description("Product catalog, pricing, SKU, and specifications"),
                        new Tag().name("Stock").description("Real-time inventory levels, stock entries, exits, and adjustments"),
                        new Tag().name("Invoices").description("Commercial billing, taxes, discounts, and invoice generation"),
                        new Tag().name("Payments").description("Invoice payment processing, status tracking, and settlements"),
                        new Tag().name("Audit").description("System audit logs, data change tracking, and access trails"),
                        new Tag().name("Notifications").description("Automated operational alerts (Low Stock, Overdue Invoices)"),
                        new Tag().name("Dashboard").description("Executive analytics, KPIs, and business metrics"),
                        new Tag().name("AI Decision Support").description("AI predictions, forecasting, and purchase recommendations")
                ));
    }
}
