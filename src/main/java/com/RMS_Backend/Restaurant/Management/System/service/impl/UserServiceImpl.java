package com.RMS_Backend.Restaurant.Management.System.service.impl;

import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.RegisterRequest;
import com.RMS_Backend.Restaurant.Management.System.dto.SyncUserRolesDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.enums.IdentityType;
import com.RMS_Backend.Restaurant.Management.System.exception.ResourceNotFoundException;
import com.RMS_Backend.Restaurant.Management.System.keycloak.KeycloakAdminClient;
import com.RMS_Backend.Restaurant.Management.System.mapping.LocationMapping;
import com.RMS_Backend.Restaurant.Management.System.mapping.RoleMapping;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserMapping;
import com.RMS_Backend.Restaurant.Management.System.mapping.UserRoleMappingMapping;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Location;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import com.RMS_Backend.Restaurant.Management.System.model.UserRoleMapping;
import com.RMS_Backend.Restaurant.Management.System.repository.AppUserRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.LocationRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.RoleRepository;
import com.RMS_Backend.Restaurant.Management.System.repository.UserRoleMappingRepository;
import com.RMS_Backend.Restaurant.Management.System.security.SecurityUtils;
import com.RMS_Backend.Restaurant.Management.System.service.AuditingService;
import com.RMS_Backend.Restaurant.Management.System.service.RoleService;
import com.RMS_Backend.Restaurant.Management.System.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final LocationRepository locationRepository;

    private final AuditingService auditingService;
    private final UserMapping userMapping;
    private final RoleMapping roleMapping;
    private final UserRoleMappingMapping userRoleMappingMapping;
    private final LocationMapping locationMapping;
    private final KeycloakAdminClient keycloakAdminClient;

    private final HttpServletRequest request;
    private final RoleService roleService;

    public UserServiceImpl(AppUserRepository appUserRepository,
                           RoleRepository roleRepository,
                           UserRoleMappingRepository userRoleMappingRepository,
                           LocationRepository locationRepository, AuditingService auditingService,
                           UserMapping userMapping,
                           RoleMapping roleMapping,
                           UserRoleMappingMapping userRoleMappingMapping,
                           LocationMapping locationMapping, KeycloakAdminClient keycloakAdminClient, HttpServletRequest request, RoleService roleService) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleMappingRepository = userRoleMappingRepository;
        this.locationRepository = locationRepository;
        this.auditingService = auditingService;
        this.userMapping = userMapping;
        this.roleMapping = roleMapping;
        this.userRoleMappingMapping = userRoleMappingMapping;
        this.locationMapping = locationMapping;
        this.keycloakAdminClient = keycloakAdminClient;
        this.request = request;
        this.roleService = roleService;
    }
    private AppUser currentUser(){
        String loginKeycloakId = SecurityUtils.getCurrentUserLogin();
        return appUserRepository.findByKeycloakSubjectId(loginKeycloakId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public AppUserDTO registerUser(RegisterRequest registerRequest) {
        Role role = registerRequest.getAppRole();
        return domainToDto(createUser(registerRequest, resolveRole(role)));
    }

    @Override
    public AppUserDTO registerImportedUser(RegisterRequest registerRequest) {
        Role role = registerRequest.getAppRole();
        return domainToDto(createImportedUser(registerRequest, resolveRole(role)));
    }

    @Override
    public AppUser createUser(RegisterRequest registerRequest, Role role) {
        AppUser user = userMapping.RequestToDomain(registerRequest);
        // Default flags for a new user
        user.setActive(true);
        user.setDeleted(false);
        // Ensure Location reference is managed if provided
        if (user.getLocation() != null && user.getLocation().getId() != null) {
            locationRepository.findById(user.getLocation().getId()).ifPresent(user::setLocation);
        }
        user = appUserRepository.save(user);

        if (role != null) {
            UserRoleMapping mapping = userRoleMappingMapping.create(user, role, true);
            userRoleMappingRepository.save(mapping);
        }
        UserRepresentation userRepresentation = createUserRepresentation(registerRequest);
        String keycloakId;
        try {
            Response response = keycloakAdminClient.createUser(userRepresentation);
            log.info("Keycloak response status: {}", response.getStatus());
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
            }
            String locationHeader = response.getHeaderString("Location");
            keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
            log.info("User created in Keycloak with ID: {}", keycloakId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user in Keycloak", e);
        }


        user.setKeycloakSubjectId(keycloakId);
        user.setActive(false);
        auditingService.setCreationAuditingFields(user);
        user = appUserRepository.save(user);
        if( registerRequest.getPassword()!=null){
            keycloakAdminClient.setPassword(keycloakId, registerRequest.getPassword());
        }

        if(role!=null ){
            SyncUserRolesDTO syncUserRolesDTO =  new SyncUserRolesDTO();
            syncUserRolesDTO.setAppUserId(user.getId());
            syncUserRolesDTO.setRoles(Collections.singletonList(role));
            roleService.syncRolesWithUser(syncUserRolesDTO);
            keycloakAdminClient.syncRolesWithUser(keycloakId,Collections.singletonList(role));
        }

        log.info("User created in Keycloak with ID: {}", keycloakId);
        return user;
    }

    @Override
    public AppUser createImportedUser(RegisterRequest registerRequest, Role role) {
        AppUser user = userMapping.RequestToDomain(registerRequest);
        // Imported users default to inactive until activation
        user.setActive(false);
        user.setDeleted(false);
        if (user.getLocation() != null && user.getLocation().getId() != null) {
            locationRepository.findById(user.getLocation().getId()).ifPresent(user::setLocation);
        }
        user = appUserRepository.save(user);

        if (role != null) {
            UserRoleMapping mapping = userRoleMappingMapping.create(user, role, false);
            userRoleMappingRepository.save(mapping);
        }
        return user;
    }

    @Override
    public AccessTokenResponse login(String identity, String identityType, String password) {
        log.info("Logging in user with identity: {}, type: {}", identity, identityType);

        AppUser loggedInUser;

        // Handle different identity types
//        if (identityType.equals(IdentityType.GOVT_ID.name())) {
//            // For GOVT_ID, we need to parse the identity string to get type and number
//            String[] parts = identity.split(":");
//            if (parts.length != 2) {
//                log.error("Invalid govt ID format. Expected 'type:number', got: {}", identity);
//                throw new ResourceNotFoundException("Invalid govt ID format", "identity", identity);
//            }
//
//            String govtIdType = parts[0];
//            String govtIdNumber = parts[1];
//
//            log.info("Looking up user by govt ID type: {} and number: {}", govtIdType, govtIdNumber);
//            loggedInUser = appUserRepository.findByGovtIdTypeAndGovtIdNumberAndIsActiveTrue(govtIdType, govtIdNumber)
//                    .orElseThrow(() -> {
//                        log.error("User not found with govt ID type: {} and number: {}", govtIdType, govtIdNumber);
//                        return new ResourceNotFoundException("User not found with govt ID", "identity", identity);
//                    });
//        } else {
            // Find the user by username or mobile number
            loggedInUser = appUserRepository.findByUsernameOrMobileNumberAndIsActiveTrue(identity, identity)
                    .orElseThrow(() -> {
                        log.error("User not found with identity: {}", identity);
                        return new ResourceNotFoundException("User not found with ", "identity", identity);
                    });


        AccessTokenResponse token = null;

        // Generate token using KeycloakAdminClient
        if (identityType.equals(IdentityType.MOBILE.name())) {
            token = keycloakAdminClient.getUserTokenByMobile(loggedInUser.getMobileNumber(), password);
            log.info("Token generated for user by mobile: {}", identity);
            }
       else {
            token = keycloakAdminClient.getUserToken(loggedInUser.getUsername(), password);
            log.info("Token generated for user by username: {}", identity);
        }

        if(token==null){
            throw new ResourceNotFoundException("Failed to generate token for: ", "identity", identity);
        }




        return token;
    }

    @Override
    public List<AppUserDTO> getAllUsers() {
        return appUserRepository.findAll().stream()
            .map(this::domainToDto)
            .collect(Collectors.toList());
    }

    @Override
    public Page<AppUserDTO> getPaginatedUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> users = appUserRepository.findAll(pageable);
        List<AppUserDTO> dtos = users.getContent().stream().map(this::domainToDto).toList();
        return new PageImpl<>(dtos, pageable, users.getTotalElements());
    }

    @Override
    public AppUserDTO getUserById(Long id) {
        return appUserRepository.findById(id).map(this::domainToDto).orElse(null);
    }

    @Override
    public AppUserDTO updateUser(Long id, AppUserDTO appUserDTO) {
        Optional<AppUser> existingOpt = appUserRepository.findById(id);
        if (existingOpt.isEmpty()) return null;
        AppUser existing = existingOpt.get();
        // Update selected fields
        if (appUserDTO.getFirstName() != null) existing.setFirstName(appUserDTO.getFirstName());
        if (appUserDTO.getLastName() != null) existing.setLastName(appUserDTO.getLastName());
        if (appUserDTO.getEmail() != null) existing.setEmail(appUserDTO.getEmail());
        if (appUserDTO.getMobileNumber() != null) existing.setMobileNumber(appUserDTO.getMobileNumber());
        if (appUserDTO.getPreferredLanguage() != null) existing.setPreferredLanguage(appUserDTO.getPreferredLanguage());
        if (appUserDTO.getLocationId() != null) {
            locationRepository.findById(appUserDTO.getLocationId()).ifPresent(existing::setLocation);
        }
        existing = appUserRepository.save(existing);
        return domainToDto(existing);
    }

    @Override
    public AppUserDTO updateUserActivation(Long id, boolean isActive) {
        Optional<AppUser> userOpt = appUserRepository.findById(id);
        if (userOpt.isEmpty()) return null;
        AppUser user = userOpt.get();
        user.setActive(isActive);
        return domainToDto(appUserRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        // Remove role mappings first to avoid constraint issues
        userRoleMappingRepository.deleteByAppUserId(userId);
        appUserRepository.deleteById(userId);
    }

    @Override
    public AppUserDTO getUserBykeycloakId(String keycloakId) {
        if (keycloakId == null) return null;
        // Repository method not defined; fallback to filter
        return appUserRepository.findAll().stream()
            .filter(u -> keycloakId.equals(u.getKeycloakSubjectId()))
            .findFirst()
            .map(this::domainToDto)
            .orElse(null);
    }

    @Override
    public AppUserDTO saveUser(AppUser appUser) {
        AppUser saved = appUserRepository.save(appUser);
        return domainToDto(saved);
    }

    @Override
    public List<AppUserDTO> getAllUsersByRole(Long roleId) {
        List<UserRoleMapping> mappings = userRoleMappingRepository.findByRoleId(roleId);
        List<AppUserDTO> dtos = new ArrayList<>();
        for (UserRoleMapping m : mappings) {
            dtos.add(domainToDto(m.getAppUser()));
        }
        return dtos;
    }

    @Override
    public Page<AppUserDTO> getPaginatedUsersByRole(Long roleId, int page, int size) {
        List<UserRoleMapping> mappings = userRoleMappingRepository.findByRoleId(roleId);
        List<AppUser> users = mappings.stream().map(UserRoleMapping::getAppUser).toList();
        int from = Math.min(page * size, users.size());
        int to = Math.min(from + size, users.size());
        List<AppUserDTO> content = users.subList(from, to).stream().map(this::domainToDto).toList();
        return new PageImpl<>(content, PageRequest.of(page, size), users.size());
    }

    @Override
    public AccessTokenResponse getAccessToken(String refreshToken) {
        AccessTokenResponse token =keycloakAdminClient.getAccessTokenFromRefreshToken(refreshToken);
        log.info("Token retrieved successfully.");
        return token;
    }

    @Override
    public AppUserDTO getMe() {
        return userMapping.domainToUserRolesDTO(currentUser());
    }

    @Override
    public AppUser getAppUserEntityBykeycloakId(String loginKeycloakId) {
        if (loginKeycloakId == null) return null;
        return appUserRepository.findAll().stream()
            .filter(u -> loginKeycloakId.equals(u.getKeycloakSubjectId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public AppUser getAppUserEntity(Long id) {
        return appUserRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public boolean loadUsersFromDatabaseToKeycloak() {
        log.info("Starting process to load users from database to Keycloak.");

        List<AppUser> localUsersToSync = appUserRepository.findByUsernameIsNotNullOrderById();

        if (localUsersToSync.isEmpty()) {
            log.info("No local users found with username. No users to create in Keycloak.");
            return false;
        }


        int i=1;
        for (AppUser localUser : localUsersToSync) {
            log.info("Processing user: {}", i+"============"+ localUser.getUsername()+"============"+localUser.getId());
            if (localUser.getUsername() == null) {
                continue;
            }

            if (!keycloakAdminClient.userExists(localUser.getUsername())) {
                try {
                    // Create user representation
                    UserRepresentation userRepresentation = new UserRepresentation();
                    userRepresentation.setUsername(localUser.getUsername());
                    userRepresentation.setEnabled(localUser.isActive());
                    userRepresentation.setEmail(localUser.getEmail());
                    userRepresentation.setFirstName(localUser.getFirstName());
                    userRepresentation.setLastName(localUser.getLastName());
                    userRepresentation.setEmailVerified(true);

                    // Password setup
                    CredentialRepresentation passwordCred = new CredentialRepresentation();
                    passwordCred.setType(CredentialRepresentation.PASSWORD);
                    passwordCred.setValue("Secure@123");
                    passwordCred.setTemporary(false);
                    userRepresentation.setCredentials(Collections.singletonList(passwordCred));

                    // Roles
                    List<Role> userRoles = roleService.getUserRoleMappingsByUser(localUser.getId())
                            .stream()
                            .map(UserRoleMapping::getRole)
                            .toList();
                    List<String> userRolesAsStrings = userRoles.stream().map(Role::getName).toList();
                    userRepresentation.setRealmRoles(userRolesAsStrings);

                    // Attributes
                    Map<String, List<String>> attributes = new HashMap<>();
                    attributes.put("mobile_number", Collections.singletonList(localUser.getMobileNumber()));
                    userRepresentation.setAttributes(attributes);

                    // Create user in Keycloak
                    Response response = keycloakAdminClient.createUser(userRepresentation);
                    log.info("Keycloak response status: {}", response.getStatus());

                    if (response.getStatus() != 201) {
                        log.error("Failed to create user '{}' in Keycloak. Status: {}", localUser.getUsername(), response.getStatus());
                        continue;
                    }
                    String keycloakId = CreatedResponseUtil.getCreatedId(response);
                    //String locationHeader = response.getHeaderString("Location");
                    //String keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);

                    if(!localUser.getKeycloakSubjectId().equals(keycloakId)){
                        localUser.setKeycloakSubjectId(keycloakId);
                        appUserRepository.save(localUser);
                    }
                    log.info("User '{}' created in Keycloak with ID: {}", localUser.getUsername(), keycloakId);

                    // Set password and roles
                    keycloakAdminClient.setPassword(keycloakId, "Secure@123");
                    keycloakAdminClient.syncRolesWithUser(keycloakId, userRoles);



                } catch (Exception e) {
                    log.error("Error creating user '{}' in Keycloak: {}", localUser.getUsername(), e.getMessage(), e);
                    return false;
                }
            }else{
                log.info("User '{}' already exists in Keycloak. Skipping creation.", localUser.getUsername());
                UserRepresentation keycloakUser = keycloakAdminClient.getUserResourceByUsername(localUser.getUsername()).toRepresentation();
                if(keycloakUser==null){
                    log.info("User '{}' already exists in Keycloak. Skipping creation.", localUser.getUsername());
                    continue;
                }
                if(keycloakUser.isEnabled()!=(localUser.isActive())){
                    localUser.setActive(keycloakUser.isEnabled());
                }
                if(!Objects.equals(keycloakUser.getId(), localUser.getKeycloakSubjectId())){
                    localUser.setKeycloakSubjectId(keycloakUser.getId());

                }if(keycloakUser.getEmail()!=null && !Objects.equals(keycloakUser.getEmail(), localUser.getEmail())){
                    localUser.setEmail(keycloakUser.getEmail());

                }if(keycloakUser.getFirstName()!=null && !Objects.equals(keycloakUser.getFirstName(), localUser.getFirstName())){
                    localUser.setFirstName(keycloakUser.getFirstName());

                }if(keycloakUser.getLastName()!=null && !Objects.equals(keycloakUser.getLastName(), localUser.getLastName())){
                    localUser.setLastName(keycloakUser.getLastName());
                }if(keycloakUser.getAttributes()!=null && keycloakUser.getAttributes().get("mobile_number")!=null && !Objects.equals(keycloakUser.getAttributes().get("mobile_number").get(0), localUser.getMobileNumber())){
                    localUser.setMobileNumber(keycloakUser.getAttributes().get("mobile_number").get(0));
                }

                appUserRepository.save(localUser);
            }
            log.info("Processed user: {}", i+"============"+ localUser.getUsername()+"============"+localUser.getId());
            i++;
        }
        log.info("Total Procesed users: {}", i);
        return true;

    }

    @Transactional
    @Override
    public boolean deleteKeycloakUsers() {
        try {
            log.info("Starting bulk deletion of Keycloak users");
            UsersResource users = keycloakAdminClient.getRealmResource().users();

            // Track total users deleted
            int totalDeleted = 0;
            int batchSize = 500; // Process users in batches of 500
            int maxIterations = 20; // Safety limit to prevent infinite loops
            int iteration = 0;
            boolean continueDeleting = true;

            while (continueDeleting && iteration < maxIterations) {
                iteration++;
                log.info("Starting deletion iteration {}/{} (Total deleted so far: {})",
                        iteration, maxIterations, totalDeleted);

                // Always start from the beginning to get the most current list of users
                // This is more reliable than using pagination with an offset
                List<UserRepresentation> usersBatch = users.list(0, batchSize);

                // Filter out superadmin
                List<UserRepresentation> usersToDelete = usersBatch.stream()
                        .filter(user -> !user.getUsername().equals("superadmin"))
                        .collect(Collectors.toList());

                int batchUsersCount = usersToDelete.size();
                log.info("Found {} users to delete in current batch", batchUsersCount);

                if (usersToDelete.isEmpty()) {
                    log.info("No more users to delete. Deletion complete.");
                    continueDeleting = false;
                    break;
                }

                // Extract user IDs for bulk deletion
                List<String> userIds = usersToDelete.stream()
                        .map(UserRepresentation::getId)
                        .collect(Collectors.toList());

                // Create a map of user IDs to usernames for logging
                Map<String, String> userIdToUsername = usersToDelete.stream()
                        .collect(Collectors.toMap(
                                UserRepresentation::getId,
                                UserRepresentation::getUsername
                        ));

                // Perform bulk deletion in parallel
                log.info("Performing parallel deletion of {} users in current batch", batchUsersCount);
                Map<String, Boolean> results = keycloakAdminClient.bulkDeleteUsersViaRestApi(userIds);

                // Count successful deletions
                long successCount = results.values().stream().filter(Boolean::booleanValue).count();
                totalDeleted += successCount;
                log.info("Batch deletion completed. Successfully deleted {}/{} users in this batch",
                        successCount, batchUsersCount);

                // If some users failed to delete, try one more time for those users
                List<String> failedUserIds = results.entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                if (!failedUserIds.isEmpty()) {
                    log.warn("{} users failed to delete in this batch. Attempting retry...", failedUserIds.size());

                    // Log the failed users (limit to first 5 to reduce log volume)
                    int logLimit = Math.min(failedUserIds.size(), 5);
                    for (int i = 0; i < logLimit; i++) {
                        String userId = failedUserIds.get(i);
                        log.warn("Failed to delete user: {} (ID: {})", userIdToUsername.get(userId), userId);
                    }

                    // Wait a bit before retrying
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    // Retry deletion for failed users
                    Map<String, Boolean> retryResults = keycloakAdminClient.bulkDeleteUsersViaRestApi(failedUserIds);
                    long retrySuccessCount = retryResults.values().stream().filter(Boolean::booleanValue).count();
                    totalDeleted += retrySuccessCount;
                    log.info("Retry completed. Successfully deleted {}/{} remaining users in this batch",
                            retrySuccessCount, failedUserIds.size());
                }

                // Add a delay between iterations to avoid overwhelming Keycloak
                try {
                    log.info("Waiting before next iteration...");
                    Thread.sleep(2000); // Increased from 1000ms to 2000ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Refresh the users resource to ensure we're working with the current state
                users = keycloakAdminClient.getRealmResource().users();
            }

            if (iteration >= maxIterations) {
                log.warn("Reached maximum number of iterations ({}). Some users may still remain.", maxIterations);
            }

            // Final verification - check if any non-superadmin users remain
            log.info("All iterations completed. Verifying deletion...");

            List<UserRepresentation> remainingUsers = users.list().stream()
                    .filter(user -> !user.getUsername().equals("superadmin"))
                    .collect(Collectors.toList());

            if (!remainingUsers.isEmpty()) {
                log.warn("{} users still remain in Keycloak after all deletion attempts", remainingUsers.size());

                // Log details of remaining users (limit to 20 to avoid excessive logging)
                int detailLimit = Math.min(remainingUsers.size(), 20);
                for (int i = 0; i < detailLimit; i++) {
                    UserRepresentation user = remainingUsers.get(i);
                    log.warn("User still exists after all attempts: {} (ID: {})", user.getUsername(), user.getId());
                }

                // If there are still many users left, try one more aggressive approach
                if (remainingUsers.size() > 20) {
                    log.info("Attempting one final aggressive deletion for {} remaining users", remainingUsers.size());

                    // Extract user IDs for bulk deletion
                    List<String> remainingUserIds = remainingUsers.stream()
                            .map(UserRepresentation::getId)
                            .collect(Collectors.toList());

                    // Perform bulk deletion with increased thread count
                    Map<String, Boolean> finalResults = keycloakAdminClient.bulkDeleteUsersViaRestApi(remainingUserIds);
                    long finalSuccessCount = finalResults.values().stream().filter(Boolean::booleanValue).count();
                    totalDeleted += finalSuccessCount;
                    log.info("Final deletion completed. Successfully deleted {}/{} remaining users",
                            finalSuccessCount, remainingUserIds.size());

                    // Check again if any users remain
                    remainingUsers = users.list().stream()
                            .filter(user -> !user.getUsername().equals("superadmin"))
                            .collect(Collectors.toList());

                    if (remainingUsers.isEmpty()) {
                        log.info("All users successfully deleted after final attempt. Total deleted: {}", totalDeleted);
                        return true;
                    } else {
                        log.warn("{} users still remain after final attempt", remainingUsers.size());
                        return false;
                    }
                }

                return false;
            }

            log.info("All users successfully deleted from Keycloak. Total deleted: {}", totalDeleted);
            return true;
        } catch (Exception e) {
            log.error("Error during bulk deletion of Keycloak users: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public AppUser save(AppUser updateUser) {
        return appUserRepository.save(updateUser);
    }

    @Override

    public AppUserDTO updateCurrentUserRoleActivation(Long roleId) {
        // Get the current user
        AppUser currentUser = currentUser();

        // Fetch all active user roles for the current user
        // List<UserRoleMapping> activeUserRoles = roleService.getActiveUserRoleMappingsByUser(currentUser.getId());
        List<UserRoleMapping> userRoleMappings = roleService.getUserRoleMappingsByUser(currentUser.getId());
        // Make all roles inactive except for the selected role
        for (UserRoleMapping userRoleMapping : userRoleMappings) {
            if (userRoleMapping.getRole().getId().equals(roleId)) {
                userRoleMapping.setActive(true);
                roleService.saveUserRoleMapping(userRoleMapping);
            }else{
                userRoleMapping.setActive(false);
                roleService.saveUserRoleMapping(userRoleMapping);
            }
        }

        // Return the updated user DTO
        return userMapping.domainToUserRolesDTO(currentUser);
    }

    @Transactional
    @Override
    public boolean deleteKeycloakUsersNotInDb() {
        log.info("Starting deletion of Keycloak users not found in the database.");
        UsersResource usersResource = keycloakAdminClient.getRealmResource().users();

        try {
            // Step 1: Fetch all users from Keycloak
            // We need to iterate through Keycloak users as list() without limits might not return all
            Set<String> keycloakUsernames = new HashSet<>();
            Map<String, String> keycloakUsernameToId = new HashMap<>(); // To get ID later
            int keycloakBatchSize = 500;
            int offset = 0;
            List<UserRepresentation> currentKeycloakBatch;
            int totalKeycloakUsersFetched = 0;

            log.info("Fetching all users from Keycloak...");
            do {
                currentKeycloakBatch = usersResource.list(offset, keycloakBatchSize);
                for (UserRepresentation user : currentKeycloakBatch) {
                    // Always exclude 'superadmin' from any deletion consideration
                    if (user.getUsername() != null && !user.getUsername().equals("superadmin")) {
                        keycloakUsernames.add(user.getUsername());
                        keycloakUsernameToId.put(user.getUsername(), user.getId());
                    }
                }
                offset += currentKeycloakBatch.size();
                totalKeycloakUsersFetched += currentKeycloakBatch.size();
                log.debug("Fetched {} Keycloak users so far. Current offset: {}", totalKeycloakUsersFetched, offset);
            } while (currentKeycloakBatch.size() == keycloakBatchSize); // Continue if the batch was full

            log.info("Finished fetching Keycloak users. Total unique Keycloak users (excluding superadmin): {}", keycloakUsernames.size());

            // Step 2: Fetch all relevant user identifiers from your application's database
            // Assuming your UserRepository has a method to get all usernames,
            // or you fetch all entities and map their usernames.
            // Replace `userRepository.findAllUsernames()` with your actual method.
            // Example: If your User entity has a 'username' field:
            Set<String> dbUsernames = appUserRepository.findAll().stream()
                    .map(AppUser::getUsername) // Assuming YourUserEntity has getUsername()
                    .collect(Collectors.toSet());
            log.info("Total users found in database: {}", dbUsernames.size());

            // Step 3: Compare the two sets and identify users for deletion
            List<String> userIdsToDelete = keycloakUsernames.stream()
                    .filter(kcUsername -> !dbUsernames.contains(kcUsername))
                    .map(keycloakUsernameToId::get) // Get the Keycloak ID for deletion
                    .collect(Collectors.toList());

            if (userIdsToDelete.isEmpty()) {
                log.info("No Keycloak users found that are not in the database. No deletion needed.");
                return true;
            }

            log.info("Found {} Keycloak users to delete (not present in database).", userIdsToDelete.size());

            // Prepare for logging names of users to be deleted
            Map<String, String> userIdToUsernameForDeletion = new HashMap<>();
            userIdsToDelete.forEach(id -> {
                String username = keycloakUsernameToId.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(id))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("UNKNOWN_USERNAME");
                userIdToUsernameForDeletion.put(id, username);
            });

            // Step 4: Perform bulk deletion (reusing logic from your existing method)
            int totalDeleted = 0;
            int deletionBatchSize = 100; // Smaller batch size for deletion if needed
            int maxDeletionRetries = 3;

            for (int i = 0; i < userIdsToDelete.size(); i += deletionBatchSize) {
                List<String> currentDeletionBatchIds = userIdsToDelete.subList(
                        i, Math.min(i + deletionBatchSize, userIdsToDelete.size()));

                log.info("Attempting to delete batch of {} Keycloak users. (Total deleted so far: {})",
                        currentDeletionBatchIds.size(), totalDeleted);

                Map<String, Boolean> results = null;
                for (int retryAttempt = 0; retryAttempt < maxDeletionRetries; retryAttempt++) {
                    try {
                        results = keycloakAdminClient.bulkDeleteUsersViaRestApi(currentDeletionBatchIds);
                        long successCount = results.values().stream().filter(Boolean::booleanValue).count();
                        log.info("Batch deletion attempt {}. Successfully deleted {}/{} users in this batch.",
                                retryAttempt + 1, successCount, currentDeletionBatchIds.size());
                        break; // Exit retry loop on success
                    } catch (Exception e) {
                        log.warn("Error during bulk deletion attempt {}: {}. Retrying...",
                                retryAttempt + 1, e.getMessage());
                        Thread.sleep(2000); // Wait before retrying
                    }
                }

                if (results != null) {
                    long successCount = results.values().stream().filter(Boolean::booleanValue).count();
                    totalDeleted += successCount;

                    List<String> failedUserIds = results.entrySet().stream()
                            .filter(entry -> !entry.getValue())
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    if (!failedUserIds.isEmpty()) {
                        log.warn("{} users failed to delete in this batch. (Remaining failures: {})", failedUserIds.size(), failedUserIds.size());
                        int logLimit = Math.min(failedUserIds.size(), 10); // Log first 10 failures
                        for (int j = 0; j < logLimit; j++) {
                            String userId = failedUserIds.get(j);
                            log.warn("Failed to delete user: {} (ID: {})", userIdToUsernameForDeletion.get(userId), userId);
                        }
                    }
                } else {
                    log.error("All retry attempts failed for current deletion batch. Some users may not be deleted.");
                }

                // Add a delay between batches to avoid overwhelming Keycloak
                try {
                    log.info("Waiting before next deletion batch...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            log.info("Finished processing Keycloak users not in DB. Total users successfully deleted: {}", totalDeleted);

            // Final verification: Check if any of the initially identified users still exist in Keycloak
            log.info("Performing final verification of deletion...");
            List<UserRepresentation> remainingKeycloakUsers = usersResource.list().stream()
                    .filter(user -> userIdsToDelete.contains(user.getId())) // Filter for those we tried to delete
                    .collect(Collectors.toList());

            if (!remainingKeycloakUsers.isEmpty()) {
                log.warn("{} users still remain in Keycloak that were supposed to be deleted (not in DB).", remainingKeycloakUsers.size());
                int logLimit = Math.min(remainingKeycloakUsers.size(), 10);
                for (int j = 0; j < logLimit; j++) {
                    UserRepresentation user = remainingKeycloakUsers.get(j);
                    log.warn("User still exists: {} (ID: {})", user.getUsername(), user.getId());
                }
                return false; // Indicate partial failure
            }

            log.info("All identified Keycloak users not in DB were successfully deleted.");
            return true;

        } catch (Exception e) {
            log.error("Error during deletion of Keycloak users not in DB: {}", e.getMessage(), e);
            return false;
        }
    }

    private AppUserDTO domainToDto(AppUser appUser) {
        return userMapping.domainToUserRolesDTO(appUser);
    }

    private Role resolveRole(Role role) {
        if (role == null) return null;
        if (role.getId() != null) {
            return roleRepository.findById(role.getId()).orElse(null);
        }
        // No repository method for name lookup; return as-is (may be unmanaged)
        return role;
    }

    private UserRepresentation createUserRepresentation(RegisterRequest registerRequest) {
        UserRepresentation userRepresentation = new UserRepresentation();

        // Check username first (not null AND not empty)
        if (registerRequest.getUsername() != null && !registerRequest.getUsername().trim().isEmpty()) {
            userRepresentation.setUsername(registerRequest.getUsername());
        }
        // If username check fails, check mobile number (not null AND not empty)
        else if (registerRequest.getMobileNumber() != null && !registerRequest.getMobileNumber().trim().isEmpty()) {
            userRepresentation.setUsername(registerRequest.getMobileNumber());
        }
        // If both username and mobile checks fail, use govt ID type and number
        else {
            log.warn("No valid identifier provided for Keycloak user creation (username, mobile number, or govt ID)");
            // Fallback to a default username if all else fails
            userRepresentation.setUsername("user_" + UUID.randomUUID().toString());
        }

        userRepresentation.setEmail(registerRequest.getEmail());

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("mobile_number", Collections.singletonList(registerRequest.getMobileNumber()));
        userRepresentation.setAttributes(attributes);

        userRepresentation.setFirstName(registerRequest.getFirstName());
        userRepresentation.setLastName(registerRequest.getLastName());
        userRepresentation.setEnabled(false);

        return userRepresentation;
    }
}