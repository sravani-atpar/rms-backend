package com.RMS_Backend.Restaurant.Management.System.config;


import com.RMS_Backend.Restaurant.Management.System.security.logging.ApiLogoutSuccessHandler;
import com.RMS_Backend.Restaurant.Management.System.security.logging.CustomLogoutHandler;
import com.RMS_Backend.Restaurant.Management.System.service.TokenDenylistService;
import com.RMS_Backend.Restaurant.Management.System.service.UserService;
import com.RMS_Backend.Restaurant.Management.System.service.impl.KeycloakUserDetailsServiceImpl;
import jakarta.servlet.Filter;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {



    private final KeycloakUserDetailsServiceImpl keycloakUserDetailsService;
    private final UserService userService;
    private final CustomLogoutHandler customLogoutHandler;
    private final ApiLogoutSuccessHandler apiLogoutSuccessHandler;
    private final TokenDenylistService tokenDenylistService;

    public SecurityConfig(KeycloakUserDetailsServiceImpl keycloakUserDetailsService, UserService userService, CustomLogoutHandler customLogoutHandler, ApiLogoutSuccessHandler apiLogoutSuccessHandler, TokenDenylistService tokenDenylistService) {
        this.keycloakUserDetailsService = keycloakUserDetailsService;
        this.userService = userService;

        this.customLogoutHandler = customLogoutHandler;
        this.apiLogoutSuccessHandler = apiLogoutSuccessHandler;
        this.tokenDenylistService = tokenDenylistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
                .csrf().disable() // Assuming CSRF is disabled for API
                .cors().and() // Enable CORS using the bean below
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers("/swagger-ui/**", "/rms-docs/**","/api/roles/**","/api/roles/**","/api/locations/**", "/swagger/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/message/**", "/api/otp-via-call").permitAll()



                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter(jwtDecoder), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                // --- ADD LOGOUT CONFIGURATION ---
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // The endpoint clients POST to for logout
                        .addLogoutHandler(customLogoutHandler) // <-- ADD YOUR CUSTOM HANDLER HERE
                        .logoutSuccessHandler(apiLogoutSuccessHandler)

                );

        return http.build();
    }


    // Ensure this converter produces authorities with the "ROLE_" prefix if needed
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of(); // Return empty list if roles claim is missing
            }

            Object rolesObject = realmAccess.get("roles");
            if (!(rolesObject instanceof List<?> rolesList)) {
                return List.of(); // Return empty list if roles claim is not a list
            }

            // Ensure the roles are treated as strings and prefixed with "ROLE_"
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) rolesList.stream()
                    .filter(String.class::isInstance)
                    .collect(Collectors.toSet()); // Use Set to avoid duplicates

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Add "ROLE_" prefix here
                    .collect(Collectors.toList()); // Return as List<GrantedAuthority>
        });
        return converter;
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new KeycloakAuthenticationProvider(); // This seems specific to an older adapter
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return keycloakUserDetailsService; // Used by your custom filter
    }

    @Bean
    public Filter jwtAuthenticationFilter(JwtDecoder jwtDecoder) {

        return new JwtAuthenticationFilter(jwtDecoder, keycloakUserDetailsService,userService,tokenDenylistService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Consider specifying allowed origins explicitly instead of "*" for better security in production
        configuration.setAllowedOrigins(List.of(

                "https://awdapi-dev.aurex.in",
                "https://awddashboard-dev.aurex.in/login",
                "https://awddashboard-dev.aurex.in",
                "https://awdapi.aurex.in",
                "https://awddashboard.aurex.in",
                "https://awd-dashboard.atparui.com",
                "https://awd-api.atparui.com",
                "https://awd-dev-dashboard.atparui.com",
                "https://awd-dev-api.atparui.com",
                "https://awd-staging-dashboard.atparui.com",
                "https://awd-staging-api.atparui.com",
                "http://localhost:3001",
                "http://localhost:8004",
                "http://localhost:3000",
                "http://localhost:8003")); // Example: List.of("http://localhost:4200", "https://yourproductiondomain.com")
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // Added PATCH
        configuration.setAllowedHeaders(List.of("*")); // Consider specifying allowed headers explicitly
        configuration.setAllowCredentials(true); // Set to true if you need to send cookies or auth headers with credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/ws/**", configuration);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
