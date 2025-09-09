package com.RMS_Backend.Restaurant.Management.System.config;




import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiKeycloakConfig {

    @Value("${rmsapp.openapi.dev-url}")
    private String defaultUrl;

    @Value("${keycloak.auth.server-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak.realm.realm-name}")
    private String keycloakRealm;

    @Bean
    public OpenAPI customOpenAPI() {



        Server server = new Server();
        server.setUrl(defaultUrl);
        server.setDescription("Development Server");

        // OAuth2 flow for Keycloak
//        OAuthFlow authorizationCodeFlow = new OAuthFlow()
//                .authorizationUrl(keycloakBaseUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/auth")
//                .tokenUrl(keycloakBaseUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token")
//                .scopes(new Scopes().addString("openid", "OpenID Connect scope"));
//
//        SecurityScheme oauthSecurityScheme = new SecurityScheme()
//                .type(SecurityScheme.Type.OAUTH2)
//                .flows(new OAuthFlows()
//                        .authorizationCode(authorizationCodeFlow)
//                );

//        return new OpenAPI()
//                .info(new Info()
//                        .title("Awd Farmers API")
//                        .version("1.0")
//                        .description("This API is secured by Keycloak (OAuth2 Authorization Code Flow)")
//                )
//                .servers(List.of(server))
//                .components(new io.swagger.v3.oas.models.Components()
//                        .addSecuritySchemes("keycloak", oauthSecurityScheme)
//                )
//                .addSecurityItem(new SecurityRequirement().addList("keycloak"));

        return new OpenAPI()
                .info(new Info()
                        .title("RMS API")
                        .version("1.0")
                        .description("This API is secured by Keycloak (OAuth2 Authorization Code Flow)")
                )
                .servers(List.of(server))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}
