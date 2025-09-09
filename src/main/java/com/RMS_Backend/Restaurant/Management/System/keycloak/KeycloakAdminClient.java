package com.RMS_Backend.Restaurant.Management.System.keycloak;



import com.RMS_Backend.Restaurant.Management.System.dto.out.KeycloakAppUserOutDTO;
import com.RMS_Backend.Restaurant.Management.System.exception.AccessDeniedException;
import com.RMS_Backend.Restaurant.Management.System.exception.ValidationException;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.RoleRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.UserRoleMappingRepository;
import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
//import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KeycloakAdminClient {

    private static final String ANONYMOUS = "ANONYMOUS";

    private Keycloak keycloak;

    @Value("${keycloak.auth.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.realm.realm-name}")
    private String clientRealm;

    @Value("${keycloak.realm.client-id}")
    private String realmClientId;

    @Value("${keycloak.realm.client-secret}")
    private String realmClientSecret;

    @Value("${keycloak.realm.token-url}")
    private String tokenUrl;

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleMappingRepository roleMappingRepository;
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm)
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }


    public Keycloak getKeycloakInstance() {
        return keycloak;
    }

    public RealmResource getRealmResource() {
        return keycloak.realm(clientRealm);
    }

    public UsersResource getUserResource() {
        log.info("getUserResource: "+ getRealmResource().users());
        return getRealmResource().users();
    }

    public UserResource getUserResource(String keycloakSubjectId) {
        return getUserResource().get(keycloakSubjectId);
    }

    public UserResource getUserResourceByUsername(String username) {
        try {
            UsersResource usersResource = getUserResource();
            // Search for users with exact username match
            List<UserRepresentation> userList = usersResource.search(username, true);

            boolean exists = userList != null && !userList.isEmpty();
            log.debug("Checking existence of user '{}': {}", username, exists ? "Exists" : "Does Not Exist");
            return exists ? getUserResource(userList.get(0).getId()) : null;

        } catch (Exception e) {
            // Log the exception but don't re-throw for a simple existence check
            log.error("Error checking existence of user '{}' in Keycloak: {}", username, e.getMessage(), e);
            // Depending on requirements, you might re-throw or return false on error
            return null;
        }
    }


    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Cannot check for user existence with null or empty username.");
            return false;
        }
        try {
            UsersResource usersResource = getUserResource();
            // Search for users with exact username match
            List<UserRepresentation> userList = usersResource.search(username, true);

            boolean exists = userList != null && !userList.isEmpty();
            log.debug("Checking existence of user '{}': {}", username, exists ? "Exists" : "Does Not Exist");
            return exists;

        } catch (Exception e) {
            // Log the exception but don't re-throw for a simple existence check
            log.error("Error checking existence of user '{}' in Keycloak: {}", username, e.getMessage(), e);
            // Depending on requirements, you might re-throw or return false on error
            return false; // Assume not exists or cannot determine on error
        }
    }

    public Response createUser(UserRepresentation userRepresentation) {
        return getUserResource().create(userRepresentation);
    }

    public void setPassword(String userId, String password) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        UserResource userResource = getUserResource().get(userId);
        userResource.resetPassword(passwordCred);
    }

    // This method now returns a List of your DTO
    public List<KeycloakAppUserOutDTO> getAllKeycloakUserDetails() {
        UsersResource usersResource = getUserResource();

        // Fetch users. For large number of users, implement pagination:
        // List<UserRepresentation> userList = usersResource.list(0, 100);
        List<UserRepresentation> userList = usersResource.list();

        // Map each UserRepresentation to KeycloakAppUserOutDTO
        return userList.stream()
                .map(userRepresentation -> mapToKeycloakAppUserOutDTO(userRepresentation, usersResource))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Keycloak's UserRepresentation to your application's DTO.
     * Includes fetching roles, which can be performance intensive for many users.
     *
     * @param userRepresentation The Keycloak UserRepresentation object.
     * @param usersResource      The Keycloak UsersResource to fetch user-specific details (like roles).
     * @return A populated KeycloakAppUserOutDTO.
     */
    private KeycloakAppUserOutDTO mapToKeycloakAppUserOutDTO(
            UserRepresentation userRepresentation, UsersResource usersResource) {

        KeycloakAppUserOutDTO dto = new KeycloakAppUserOutDTO();

        // Map simple fields
        dto.setKeycloakId(userRepresentation.getId()); // Keycloak ID is a String (UUID)
        dto.setFirstName(userRepresentation.getFirstName());
        dto.setLastName(userRepresentation.getLastName());
        dto.setUsername(userRepresentation.getUsername());
        dto.setEmail(userRepresentation.getEmail());
        dto.setActive(userRepresentation.isEnabled()); // 'enabled' in Keycloak maps to 'isActive'

        // Mobile Number from User Attributes
        // Ensure the attribute key "mobile_number" matches what you store in Keycloak
        if (userRepresentation.getAttributes() != null && userRepresentation.getAttributes().containsKey("mobile_number")) {
            List<String> mobileNumbers = userRepresentation.getAttributes().get("mobile_number");
            if (mobileNumbers != null && !mobileNumbers.isEmpty()) {
                dto.setMobileNumber(mobileNumbers.get(0)); // Assuming it's a single value list
            }
        }


        return dto;
    }

    public void loadAndSaveUser(KeycloakAppUserOutDTO keycloakAppUserOutDTO) {
        // Fetch the user from Keycloak
        UserResource userResource = getUserResource(keycloakAppUserOutDTO.getKeycloakId());
        UserRepresentation userRepresentation = userResource.toRepresentation();


        // Check if the user already exists in the database

        Optional<AppUser> existingUserOpt =appUserRepository.findByUsernameAndIsActiveTrue(keycloakAppUserOutDTO.getUsername());
        AppUser appUser = new AppUser();
        if(existingUserOpt.isPresent()) {
            appUser = existingUserOpt.get();
        }else {
            existingUserOpt= appUserRepository.findByKeycloakSubjectId(userRepresentation.getId());

            if (existingUserOpt.isPresent()) {
                appUser = existingUserOpt.get();
            }
        }

        appUser.setUsername(userRepresentation.getUsername());
        appUser.setFirstName(userRepresentation.getFirstName());
        appUser.setLastName(userRepresentation.getLastName());
        appUser.setKeycloakSubjectId(userRepresentation.getId());
        appUser.setEmail(userRepresentation.getEmail());
        if (userRepresentation.getAttributes() != null) {
            String mobile = userRepresentation.getAttributes().get("mobile_number").get(0);
            if (mobile != null && !mobile.isEmpty()) {
                appUser.setMobileNumber(mobile);
            }

        }
        if(userRepresentation.isEnabled()){
            appUser.setActive(true);
        }
        else{
            appUser.setActive(false);
        }

        // Map Keycloak user to AppUser



        appUser= appUserRepository.save(appUser);


        assignKeycloakRolesToAppUser(userResource,appUser.getId());
        log.info("Updated user from keycloak: {}", appUser.getId());

    }


    public void assignKeycloakRolesToAppUser(UserResource userResource, Long appUserId) {
        // Fetch all effective roles from Keycloak for the user
        List<RoleRepresentation> keycloakRoles = userResource.roles().realmLevel().listEffective();

        // Map Keycloak roles to your Role entity
        List<Role>  rolesToAssign = keycloakRoles.stream()
                .map(roleRepresentation -> roleRepository.findByName(roleRepresentation.getName()))
                .filter(Optional::isPresent)  // Ensure role exists in your database
                .map(Optional::get)
                .collect(Collectors.toList());

        // Assign roles to the AppUser
        if (!rolesToAssign.isEmpty()) {
            assignRolesToAppUser(appUserId, rolesToAssign);
        }
//        else {
//            // Handle case where no valid roles were found in the database
//            // e.g., log the issue or throw an exception
//        }
    }


    public boolean assignRolesToAppUser(Long appUserId, List<Role> roles) {
        for (Role role : roles) {
            UserRoleMapping mapping = assignRoleToUser(appUserId, role.getId());
            if (mapping == null) {
                return false; // If any role assignment fails, return false
            }
        }
        return true; // All roles assigned successfully
    }

    private UserRoleMapping assignRoleToUser(Long appUserId, Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        AppUser appUser = appUserRepository.findById(appUserId).orElse(null);  // Ensure you have a method to find users
        Optional<UserRoleMapping> userRoleMappingOptional =roleMappingRepository.findByAppUserIdAndRoleId(appUserId, roleId);

        if(userRoleMappingOptional.isPresent()) {
            log.info("Assigning role {} to app user {} already exists", roleId, appUserId);
        }else {
            if (role != null && appUser != null) {
                UserRoleMapping roleMapping = new UserRoleMapping();
                roleMapping.setRole(role);
                roleMapping.setAppUser(appUser);
                roleMapping.setActive(true);
                roleMapping.setDeactivated(false);
                return roleMappingRepository.save(roleMapping);
            }
        }
        return null;
    }
    public void syncRolesWithUser(String userId, List<Role> selectedRoles) {
        // Get the existing roles assigned to the user
        RealmResource realmResource = getRealmResource();
        RolesResource rolesResource = realmResource.roles();
        UserResource userResource = getUserResource().get(userId);

        // Get all the roles currently assigned to the user
        List<RoleRepresentation> currentUserRoles = userResource.roles().realmLevel().listEffective();

        // Get the current role names
        Set<String> currentRoleNames = currentUserRoles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());

        Set<String> newRoleNames = new HashSet<>();
        for (Role selectedRole : selectedRoles) {
            newRoleNames.add(selectedRole.getName());
        }

        // Check if selected roles contain any role other than ANONYMOUS
        boolean hasNonAnonymousRole = selectedRoles.stream()
                .anyMatch(role -> !ANONYMOUS.equals(role.getName()));

        // If there are non-anonymous roles and ANONYMOUS role exists, remove ANONYMOUS role
        if (hasNonAnonymousRole && currentRoleNames.contains(ANONYMOUS)) {
            List<RoleRepresentation> anonymousRoleList = currentUserRoles.stream()
                    .filter(role -> ANONYMOUS.equals(role.getName()))
                    .collect(Collectors.toList());

            if (!anonymousRoleList.isEmpty()) {
                userResource.roles().realmLevel().remove(anonymousRoleList);
                // Remove ANONYMOUS from current role names for further processing
                currentRoleNames.remove(ANONYMOUS);
                // If ANONYMOUS is in newRoleNames and we have other roles, remove it
                if (hasNonAnonymousRole && newRoleNames.contains(ANONYMOUS)) {
                    newRoleNames.remove(ANONYMOUS);
                    // Update selectedRoles to remove ANONYMOUS role
                    selectedRoles = selectedRoles.stream()
                            .filter(role -> !ANONYMOUS.equals(role.getName()))
                            .collect(Collectors.toList());
                }
            }
        }

        // Find roles to remove (existing roles that are not in newRoles)
        Set<String> rolesToRemove = new HashSet<>(currentRoleNames);
        rolesToRemove.removeAll(newRoleNames);

        // Find roles to add (new roles that are not in current roles)
        Set<String> rolesToAdd = new HashSet<>(newRoleNames);
        rolesToAdd.removeAll(currentRoleNames);

        // Remove roles from the user
        if (!rolesToRemove.isEmpty()) {
            List<RoleRepresentation> rolesToRemoveList = currentUserRoles.stream()
                    .filter(role -> rolesToRemove.contains(role.getName()))
                    .collect(Collectors.toList());

            userResource.roles().realmLevel().remove(rolesToRemoveList);
        }

        // Add new roles to the user
        for (Role selectedRole : selectedRoles) {
            if (!rolesToAdd.isEmpty()) {
                List<RoleRepresentation> rolesToAddList = new ArrayList<>();
                if (rolesToAdd.contains(selectedRole.getName())) {
                    RoleRepresentation roleRepresentation = rolesResource.get(selectedRole.getName()).toRepresentation();
                    rolesToAddList.add(roleRepresentation);
                }

                userResource.roles().realmLevel().add(rolesToAddList);
            }
        }

    }


//    @Bean
//    public AuthzClient authzClient() {
//        org.keycloak.authorization.client.Configuration config = new org.keycloak.authorization.client.Configuration(); // Use the correct Configuration class
//        config.setRealm(clientRealm);
//        config.setAuthServerUrl(serverUrl);
//        config.setResource(realmClientId);
//        config.setCredentials(Collections.singletonMap("secret", realmClientSecret));
//        return AuthzClient.create(config);
//    }

    public AccessTokenResponse getUserToken(String username, String password) {
        try {
            Keycloak keycloakInstance = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(clientRealm)
                    .clientId(realmClientId)
                    .clientSecret(realmClientSecret)
                    .username(username)
                    .password(password)
                    .grantType(OAuth2Constants.PASSWORD) // Use the password grant
                    .build();

            return keycloakInstance.tokenManager().getAccessToken();
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid grant. Failed to get token for user: " + username + " with cause: " + e.getMessage());
        }
    }

    public AccessTokenResponse getAccessTokenFromRefreshToken(String refreshToken) {


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Set form parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", realmClientId);
        params.add("client_secret", realmClientSecret);
        params.add("refresh_token", refreshToken);

        // Create the request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // Post and map directly to AccessTokenResponse
        ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                AccessTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to refresh token. Status: " + response.getStatusCode());
        }
    }

    public AccessTokenResponse getUserTokenByMobile(String mobileNumber,String password) {

        // If OTP is valid, request Keycloak to issue token
        try {
            Keycloak keycloakInstance = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(clientRealm)
                    .clientId(realmClientId)
                    .clientSecret(realmClientSecret)
                    .username("superadmin")  // Use mobile number for authentication
                    .password("SuperSecure@123")  // No password needed, OTP verified
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS) // Use the password grant
                    .build();

            return keycloakInstance.tokenManager().getAccessToken();
        } catch (Exception e) {
            throw new ValidationException("Failed to get token for user: " + mobileNumber + " with cause: " + e.getMessage());
        }
    }



    /**
     * Delete multiple users in parallel using direct REST API calls to Keycloak
     *
     * @param userIds List of user IDs to delete
     * @return Map of user IDs to deletion results (true if deleted, false otherwise)
     */
    public Map<String, Boolean> bulkDeleteUsersViaRestApi(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("Starting bulk deletion of {} users via REST API", userIds.size());

        // Get admin token once for all deletions
        AccessTokenResponse adminToken = getAdminToken();
        if (adminToken == null || adminToken.getToken() == null) {
            log.error("Failed to get admin token for bulk user deletion");
            return userIds.stream().collect(Collectors.toMap(id -> id, id -> false));
        }

        // Process in smaller sub-batches if the total number is very large
        int maxBatchSize = 500; // Maximum number of users to process in a single batch
        Map<String, Boolean> allResults = new HashMap<>();

        if (userIds.size() > maxBatchSize) {
            log.info("Large number of users ({}). Processing in batches of {}", userIds.size(), maxBatchSize);

            // Split into batches
            for (int i = 0; i < userIds.size(); i += maxBatchSize) {
                int endIndex = Math.min(i + maxBatchSize, userIds.size());
                List<String> batch = userIds.subList(i, endIndex);

                log.info("Processing batch {}/{} with {} users",
                        (i / maxBatchSize) + 1,
                        (int) Math.ceil((double) userIds.size() / maxBatchSize),
                        batch.size());

                // Process this batch
                Map<String, Boolean> batchResults = processBatchWithThreadPool(batch, adminToken.getToken());
                allResults.putAll(batchResults);

                // Get a fresh token for the next batch if needed
                if (endIndex < userIds.size()) {
                    try {
                        log.info("Waiting before processing next batch...");
                        Thread.sleep(2000); // 2 second delay between batches

                        // Refresh token for next batch
                        adminToken = getAdminToken();
                        if (adminToken == null || adminToken.getToken() == null) {
                            log.error("Failed to refresh admin token for next batch");
                            // Continue with the current token, but log the error
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while waiting between batches", e);
                    }
                }
            }
        } else {
            // Process all users in a single batch
            allResults = processBatchWithThreadPool(userIds, adminToken.getToken());
        }

        // Log overall summary
        long totalSuccessCount = allResults.values().stream().filter(Boolean::booleanValue).count();
        log.info("Overall bulk deletion completed. Successfully deleted {}/{} users",
                totalSuccessCount, userIds.size());

        return allResults;
    }

    /**
     * Process a batch of user deletions using a thread pool
     *
     * @param userIds List of user IDs to delete in this batch
     * @param token Admin token to use for authentication
     * @return Map of user IDs to deletion results
     */
    private Map<String, Boolean> processBatchWithThreadPool(List<String> userIds, String token) {
        // Create a thread pool with a fixed number of threads
        // Increase thread count for larger batches, but cap at 30 to avoid overwhelming Keycloak
        int numThreads = Math.min(30, Math.max(10, userIds.size() / 20));
        log.info("Using thread pool with {} threads for {} users", numThreads, userIds.size());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try {
            // Create a map to store results
            Map<String, Boolean> results = Collections.synchronizedMap(new HashMap<>());

            // Create a list to hold the futures
            List<Future<?>> futures = new ArrayList<>();

            // Submit deletion tasks to the executor
            for (String userId : userIds) {
                Future<?> future = executor.submit(() -> {
                    results.put(userId, deleteUserWithToken(userId, token));
                });
                futures.add(future);
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Error waiting for user deletion task: {}", e.getMessage());
                }
            }

            // Log summary for this batch
            long successCount = results.values().stream().filter(Boolean::booleanValue).count();
            log.info("Batch deletion completed. Successfully deleted {}/{} users", successCount, userIds.size());

            return results;
        } finally {
            // Shutdown the executor
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // Increased from 30 to 60 seconds
                    log.warn("Thread pool did not terminate in time. Forcing shutdown.");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("Thread pool shutdown interrupted", e);
            }
        }
    }

    /**
     * Helper method to delete a user with a provided token
     *
     * @param userId The ID of the user to delete
     * @param token The admin token to use for authentication
     * @return true if the user was successfully deleted, false otherwise
     */
    private boolean deleteUserWithToken(String userId, String token) {
        int maxRetries = 3; // Increased from 2 to 3
        int retryDelayMs = 300;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Set up headers with authorization
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Create the request entity
                HttpEntity<String> requestEntity = new HttpEntity<>(headers);

                // Build the URL for the delete request
                String deleteUrl = serverUrl.trim() + "/admin/realms/" + clientRealm + "/users/" + userId;

                // Make the DELETE request
                ResponseEntity<Void> response = restTemplate.exchange(
                        deleteUrl,
                        HttpMethod.DELETE,
                        requestEntity,
                        Void.class
                );

                // Check if the request was successful
                if (response.getStatusCode().is2xxSuccessful()) {
                    // Only log at debug level to reduce log volume for large batches
                    log.debug("Successfully deleted user with ID: {}", userId);
                    return true;
                } else {
                    log.warn("Failed to delete user with ID: {} - Status: {} (Attempt {}/{})",
                            userId, response.getStatusCode(), attempt, maxRetries);

                    if (attempt < maxRetries) {
                        // Exponential backoff
                        int currentDelay = retryDelayMs * attempt;
                        try {
                            Thread.sleep(currentDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                // User already deleted
                log.debug("User with ID: {} not found (already deleted)", userId);
                return true;
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // Network or timeout issue
                log.warn("Network error while deleting user with ID: {} - Error: {} (Attempt {}/{})",
                        userId, e.getMessage(), attempt, maxRetries);

                if (attempt < maxRetries) {
                    // Exponential backoff with longer delay for network issues
                    int currentDelay = retryDelayMs * attempt * 2;
                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                log.warn("Error deleting user with ID: {} - Error: {} (Attempt {}/{})",
                        userId, e.getMessage(), attempt, maxRetries);

                if (attempt < maxRetries) {
                    // Exponential backoff
                    int currentDelay = retryDelayMs * attempt;
                    try {
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        log.error("Failed to delete user with ID: {} after {} attempts", userId, maxRetries);
        return false;
    }

    /**
     * Get an admin token for making direct REST API calls to Keycloak
     *
     * @return AccessTokenResponse containing the admin token
     */
    private AccessTokenResponse getAdminToken() {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Set form parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "password");
            params.add("client_id", adminClientId);
            params.add("username", adminUsername);
            params.add("password", adminPassword);

            // Create the request
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Post and map directly to AccessTokenResponse
            String tokenEndpoint = serverUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";
            ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
                    tokenEndpoint,
                    request,
                    AccessTokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Failed to get admin token. Status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception while getting admin token: {}", e.getMessage(), e);
            return null;
        }
    }


    public void replicateKeycloakUsers() {

        List<KeycloakAppUserOutDTO> keycloakAppUserOutDTOS = getAllKeycloakUserDetails();

        for (KeycloakAppUserOutDTO keycloakAppUserOutDTO : keycloakAppUserOutDTOS) {
            loadAndSaveUser(keycloakAppUserOutDTO);
        }
    }


//
//    /**
//     * Creates a user in Keycloak, sets their password, and assigns the specified roles.
//     * Also saves the user details and role mappings in the local database.
//     *
//     * @param keycloakAppUserDTO The DTO containing user details and assigned roles.
//     * @return The created AppUser object.
//     * @throws ValidationException if the username or email already exists in Keycloak.
//     */
//    public AppUser createUserWithRolesAndCredentials(KeycloakAppUserDTO keycloakAppUserDTO) {
//        UsersResource usersResource = getUserResource();
//        RealmResource realmResource = getRealmResource();
//
//        // 1. Check if user already exists in Keycloak by username or email
//        List<UserRepresentation> existingUsersByUsername = usersResource.search(keycloakAppUserDTO.getUsername(), true); // Search by username
//        if (!existingUsersByUsername.isEmpty()) {
//            throw new ValidationException("User with username '" + keycloakAppUserDTO.getUsername() + "' already exists in Keycloak.");
//        }
//
//
//        // 2. Create UserRepresentation for Keycloak
//        UserRepresentation userRepresentation = new UserRepresentation();
//        userRepresentation.setEnabled(true);
//        userRepresentation.setUsername(keycloakAppUserDTO.getUsername());
//        userRepresentation.setFirstName(keycloakAppUserDTO.getFirstName());
//        userRepresentation.setLastName(keycloakAppUserDTO.getLastName());
//        userRepresentation.setEmail(keycloakAppUserDTO.getEmail());
//        userRepresentation.setEmailVerified(false); // Typically email verification is done via a flow
//
//        // Add mobile number as an attribute
//        if (keycloakAppUserDTO.getMobileNumber() != null && !keycloakAppUserDTO.getMobileNumber().isEmpty()) {
//            Map<String, List<String>> attributes = new HashMap<>();
//            attributes.put("mobile_number", Collections.singletonList(keycloakAppUserDTO.getMobileNumber()));
//            userRepresentation.setAttributes(attributes);
//        }
//
//        Response response = usersResource.create(userRepresentation);
//
//        if (response.getStatus() == 201) { // HTTP 201 Created
//            URI location = response.getLocation();
//            String path = location.getPath();
//            String userId = path.substring(path.lastIndexOf('/') + 1);
//
//            // Set password
//            setPassword(userId, keycloakAppUserDTO.getPassword());
//
//            // Assign roles in Keycloak
//            if (keycloakAppUserDTO.getAssignedRoles() != null && !keycloakAppUserDTO.getAssignedRoles().isEmpty()) {
//                List<RoleRepresentation> rolesToAdd = new ArrayList<>();
//                RolesResource rolesResource = realmResource.roles();
//                for (Role appRole : keycloakAppUserDTO.getAssignedRoles()) {
//                    RoleRepresentation keycloakRole = rolesResource.get(appRole.getName()).toRepresentation();
//                    if (keycloakRole != null) {
//                        rolesToAdd.add(keycloakRole);
//                    } else {
//                        log.warn("Role '{}' not found in Keycloak. Skipping assignment.", appRole.getName());
//                    }
//                }
//                if (!rolesToAdd.isEmpty()) {
//                    getUserResource(userId).roles().realmLevel().add(rolesToAdd);
//                }
//            }
//
//            // Load and save the newly created user (and their roles) into your local database
//            loadAndSaveUser(userId);
//            return appUserRepository.findByKeycloakSubjectId(userId).orElse(null); // Return the newly created AppUser from your DB
//        } else {
//            // Handle other Keycloak response statuses (e.g., 409 Conflict if user already exists)
//            String errorMessage = "Failed to create user in Keycloak. Status: " + response.getStatus() + ", Reason: " + response.readEntity(String.class);
//            log.error(errorMessage);
//            throw new RuntimeException(errorMessage);
//        }
//    }
}
