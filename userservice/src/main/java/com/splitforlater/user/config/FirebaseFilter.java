package com.splitforlater.user.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class FirebaseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 1. Validation: Check if header is missing or doesn't start with Bearer
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        try {
            // 2. Verify with Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decodedToken.getUid();

            // 3. Map Firebase User to Spring Security Principal
            // We use SimpleGrantedAuthority for roles (e.g., ROLE_USER)
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    uid, // Principal
                    null, // Credentials (not needed after verification)
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. Set the Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Firebase token verified for UID: {}", uid);

        } catch (FirebaseAuthException e) {
            log.error("Firebase authentication failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Firebase Token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
