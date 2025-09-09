package com.RMS_Backend.Restaurant.Management.System.config;


import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.service.TokenDenylistService;
import com.RMS_Backend.Restaurant.Management.System.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final TokenDenylistService tokenDenylistService; // <-- 1. INJECT THE DENYLIST SERVICE

    // --- 2. UPDATE THE CONSTRUCTOR ---
    public JwtAuthenticationFilter(JwtDecoder jwtDecoder, UserDetailsService userDetailsService, UserService userService, TokenDenylistService tokenDenylistService) {
        this.jwtDecoder = jwtDecoder;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.tokenDenylistService = tokenDenylistService; // <-- Assign it
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException { // <-- Removed the outer try-catch for clarity



        String remoteAddr = request.getRemoteAddr();
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            logger.info("Request from IP (X-Forwarded-For): " + xForwardedFor + ", Remote Address: " + remoteAddr + " for URL: " + request.getRequestURI());
        } else {
            logger.info("Request from IP (Remote Address): " + remoteAddr + " for URL: " + request.getRequestURI());
        }
        String token = extractToken(request);

        if (token != null) {
            try {
                // Decode the JWT token
                Jwt jwt = jwtDecoder.decode(token);

                // --- 3. NEW: Check if the token is on the denylist ---
                String jti = jwt.getId();
                if (tokenDenylistService.isTokenDenied(jti)) {
                    // This token was part of a logout. Reject it.
                    logger.warn("Authentication failed: Token with JTI [{}] has been revoked: "+ jti);
                    throw new BadCredentialsException("Token has been revoked.");
                }
                // --- END OF NEW LOGIC ---

                // Extract the username from the token
                String username = jwt.getSubject();

                // Load user details from the service
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // (Your existing checks are good)
                AppUserDTO user = userService.getUserBykeycloakId(userDetails.getUsername());
                if (user.isDeleted()) {
                    throw new AccessDeniedException("User account is deleted.");
                }
                if (!user.isActive()) {
                    throw new AccessDeniedException("User account is inactive.");
                }

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in the context
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // All exceptions (bad signature, expired, denied, etc.) end up here.
                logger.error("Authentication failed: " + e.getMessage());
                // Clear the context to be safe
                SecurityContextHolder.clearContext();
                // Send a 401 Unauthorized response and stop the filter chain.
                // This is better than throwing ServletException.
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + e.getMessage() + "\"}");
                return; // Stop processing
            }
        }

        // Continue the filter chain
        try{
            filterChain.doFilter(request, response);
        }catch (Exception e){
            logger.error("Authentication failed: " + e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}