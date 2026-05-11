package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .filter(c -> c.getAuthentication() != null && c.getAuthentication().getPrincipal() instanceof Jwt)
                .map(c -> (Jwt) c.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
                    // Extract data from the JWT.
                    // Note: Ensure your Auth Provider includes the internal UUID in the token claims,
                    // or use the email as the primary lookup.
                    String userId = jwt.getClaimAsString("user_id"); // Or "sub"
                    String email = jwt.getClaimAsString("email");
                    String name = jwt.getClaimAsString("name");

                    // Mutate the request to append the headers
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r.headers(headers -> {
                                if (userId != null) headers.add("X-User-Id", userId);
                                if (email != null) headers.add("X-User-Email", email);
                                if (name != null) headers.add("X-User-Name", name);
                            }))
                            .build();

                    return chain.filter(mutatedExchange);
                })
                // If no valid context (e.g., public endpoints), just continue
                .switchIfEmpty(chain.filter(exchange));
    }
}
