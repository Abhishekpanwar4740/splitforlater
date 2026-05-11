package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for API Gateways
                .authorizeExchange(exchanges -> exchanges
                        // 1. WHITELIST ALL SWAGGER/OPENAPI ENDPOINTS
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/aggregate/**" // Used by SpringDoc to group microservices
                        ).permitAll()
                        // 2. REQUIRE AUTHENTICATION FOR EVERYTHING ELSE
                        .anyExchange().authenticated()
                )
                // 3. ENABLE OAUTH2 JWT VALIDATION (Pulls from your application.yml)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }
}