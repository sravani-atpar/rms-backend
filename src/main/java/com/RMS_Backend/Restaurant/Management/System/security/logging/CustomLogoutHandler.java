package com.RMS_Backend.Restaurant.Management.System.security.logging;


import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.UserActivityLog;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.service.TokenDenylistService;
import com.RMS_Backend.Restaurant.Management.System.service.UserActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor // Using Lombok for constructor injection
public class CustomLogoutHandler implements LogoutHandler {

    private final UserActivityLogService userActivityLogService;
    private final AppUserRepository appUserRepository; // Inject your repository
    private final JwtDecoder jwtDecoder;
    private final TokenDenylistService tokenDenylistService;

//    @Override
//    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//        // This 'authentication' object is the one we need! It's available here.
//
//        AppUser loggedOutUser = null;
//        String keycloakSubject = null;
//        if (authentication == null) {
//            String token =extractToken(request);
//            if(token == null) {
//                log.warn("CustomLogoutHandler called but no authentication object found.");
//                return;
//            }else{
//                Jwt jwt = jwtDecoder.decode(token);
//                 keycloakSubject = jwt.getSubject();
//                loggedOutUser = appUserRepository.findByKeycloakSubjectId(keycloakSubject).orElse(null);
//            }
//        }else{
//            Object principal = authentication.getPrincipal();
//
//            // Example: If your principal is a KeycloakPrincipal
//            if (principal instanceof KeycloakPrincipal) {
//                @SuppressWarnings("unchecked")
//                KeycloakPrincipal<KeycloakSecurityContext> kp =
//                        (KeycloakPrincipal<org.keycloak.KeycloakSecurityContext>) principal;
//                keycloakSubject = kp.getKeycloakSecurityContext().getToken().getSubject();
//                loggedOutUser = appUserRepository.findByKeycloakSubjectId(keycloakSubject).orElse(null);
//            }else {
//                log.warn("AppUser entity not found for logging LOGOUT activity. Principal type: {}. Keycloak Subject: {}",
//                        principal.getClass().getName(), keycloakSubject);
//            }
//
//        }
//
//
//        try {
//
//            // Add other checks for different principal types if needed...
//            if (loggedOutUser != null) {
//                // --- Retrieve Request Details ---
//                String ipAddress = request.getRemoteAddr();
//                String userAgent = request.getHeader("User-Agent");
//                HttpSession session = request.getSession(false);
//                String sessionId = (session != null) ? session.getId() : null;
//
//                // --- Record the LOGOUT activity ---
//                userActivityLogService.recordActivity(
//                        loggedOutUser,
//                        UserActivityLog.UserActivityType.LOGOUT,
//                        sessionId,
//                        ipAddress,
//                        userAgent
//                );
//                log.info("Successfully logged LOGOUT activity for user ID: {}", loggedOutUser.getId());
//
//            } else {
//                log.warn("AppUser entity not found for logging LOGOUT activity. Keycloak Subject: {}",
//                         keycloakSubject);
//            }
//
//        } catch (Exception e) {
//            // This prevents a failure in logging from breaking the logout flow.
//            log.error("Failed to record LOGOUT activity log during logout", e);
//        }
//    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        AppUser loggedOutUser = null;
        String keycloakSubject = null;
        String token = extractToken(request);
        String jti = null;
        long exp = 0;

        if (token == null) {
            log.warn("Logout handler called but no token found in request.");
            return;
        }

        // Always decode the token to get its properties for the denylist
        try {
            Jwt jwt = jwtDecoder.decode(token);
            keycloakSubject = jwt.getSubject();
            jti = jwt.getId();
            exp = jwt.getExpiresAt().getEpochSecond();
            loggedOutUser = appUserRepository.findByKeycloakSubjectId(keycloakSubject).orElse(null);
        } catch (Exception e) {
            log.error("Could not decode token during logout: {}", e.getMessage());
            return; // Can't proceed without a valid token
        }

        // --- Record the LOGOUT activity ---
        try {
            if (loggedOutUser != null) {
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                HttpSession session = request.getSession(false);
                String sessionId = (session != null) ? session.getId() : null;

                userActivityLogService.recordActivity(
                        loggedOutUser,
                        UserActivityLog.UserActivityType.LOGOUT,
                        sessionId,
                        ipAddress,
                        userAgent
                );
                log.info("Successfully logged LOGOUT activity for user ID: {}", loggedOutUser.getId());
            } else {
                log.warn("AppUser entity not found for logging LOGOUT activity. Keycloak Subject: {}", keycloakSubject);
            }

            // --- 2. NEW: Add token to denylist ---
            if (jti != null && exp > 0) {
                long nowInSeconds = System.currentTimeMillis() / 1000;
                long remainingLifetime = exp - nowInSeconds;

                if (remainingLifetime > 0) {
                    tokenDenylistService.addToDenylist(jti, remainingLifetime);
                    log.info("Token JTI [{}] added to denylist for {} seconds.", jti, remainingLifetime);
                }
            }
            // --- END OF NEW LOGIC ---

        } catch (Exception e) {
            log.error("Failed to process logout activity or denylist for subject {}: {}", keycloakSubject, e.getMessage(), e);
        }
    }



    private String extractToken(HttpServletRequest request) {
        // Extract the token from the Authorization header
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}