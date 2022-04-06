package com.example.iotcore.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "IOT-CORE APP",
                description = "IOT Core API",
                contact = @Contact(name = "IOT-CORE",
                        url = "http://localhost:8080/",
                        email = "iot-core@gmail.com"),
                license = @License(name = "GPL3 Licence",
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html")),
        servers = @Server(url = "http://localhost:8080")
)
@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(
                        new Components().addSecuritySchemes(
                                "bearer-key", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

    @Bean
    public GroupedOpenApi mainEndPointsOpenApi() {
        String[] paths = {"/api/**"};
        String[] packagesToScan = {"com.example.iotcore.web.controller"};
        return GroupedOpenApi
                .builder()
                .group("Main API")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .build();
    }


    @Bean
    public GroupedOpenApi userAndAuthenticationOpenApi() {
        String[] paths = {"/api/**"};
        String[] packagesToScan = {"com.example.iotcore.security.controller"};
        return GroupedOpenApi
                .builder()
                .group("Users and authentication API")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .build();
    }


    @Bean
    public GroupedOpenApi applicationManagementAndMonitoringOpenApi() {
        String[] paths = {"/management/**"};
        return GroupedOpenApi
                .builder()
                .group("Management and Monitoring")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearer-key");
                    if (!handlerMethod.toString().equals("Actuator web endpoint 'info'"))
                        operation.addSecurityItem(securityRequirement);

                    return operation;
                })
                .pathsToMatch(paths)
                .build();
    }

}
