package com.RMS_Backend.Restaurant.Management.System.controller;

import com.RMS_Backend.Restaurant.Management.System.dto.ApiResponse;
import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<List<AppUserDTO>>> getAllUsers() {
        log.debug("Entering getAllUsers");
        List<AppUserDTO> users = userService.getAllUsers();
        if (users.isEmpty()){
            log.warn("No users found");
        }
        log.info("Returning {} users", users.size());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/admin/users/paginated")
    public ResponseEntity<ApiResponse<Page<AppUserDTO>>> getPaginatedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Entering getPaginatedUsers with page: {} and size: {}", page, size);
        Page<AppUserDTO> userPage = userService.getPaginatedUsers(page, size);

        if (userPage.isEmpty()) {
            log.warn("No users found in requested page");
        }

        log.info("Returning page {} with {} users", page, userPage.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userPage));
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<AppUserDTO>> getUserById(@PathVariable Long id) {
        log.debug("Entering get user by id: {}",id);
        AppUserDTO user = userService.getUserById(id);
        log.info("Returning {} user", user);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", user));
    }

    @GetMapping("/admin/users/role/{roleId}")
    public ResponseEntity<ApiResponse<List<AppUserDTO>>> getAllUsersByRole(@PathVariable Long roleId) {
        log.debug("Entering getAllUsers by Role with roleId: {}", roleId);
        List<AppUserDTO> users = userService.getAllUsersByRole(roleId);
        if (users.isEmpty()){
            log.warn("No users found for the roleId: {}", roleId);
        }
        log.info("Returning {} users with requested roleId: {}", users.size(), roleId);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/admin/users/role/paginated/{roleId}")
    public ResponseEntity<ApiResponse<Page<AppUserDTO>>> getPaginatedUsersByRole(
            @PathVariable Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Entering getPaginatedUsersByRole with roleId: {}, page: {}, size: {}", roleId, page, size);
        Page<AppUserDTO> userPage = userService.getPaginatedUsersByRole(roleId, page, size);

        if (userPage.isEmpty()) {
            log.warn("No users found for the roleId: {}", roleId);
        }

        log.info("Returning {} users for roleId: {}", userPage.getContent().size(), roleId);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userPage));
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<AppUserDTO>> updateUser(@PathVariable Long id, @RequestBody AppUserDTO appUserDTO ) {
        log.debug("Entering updateUser with ID: {}, user: {}", id, appUserDTO);

        AppUserDTO updatedUser = userService.updateUser(id, appUserDTO);
        log.info("User updated successfully with ID: {}", updatedUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        log.debug("Entering deleteUser with ID: {}", id);
        userService.deleteUser(id);
        log.info("User deleted successfully with ID: {}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("User deleted successfully"));
    }

    @PutMapping("/admin/users/{id}/activate")
    public ResponseEntity<ApiResponse<AppUserDTO>> updateUserActivation(@PathVariable Long id, @RequestParam boolean isActive){
        log.debug("Entering updateUserActivation with ID: {}, isActive: {}", id, isActive);
        AppUserDTO updatedUser = userService.updateUserActivation(id,isActive);
        log.info("User activation updated successfully with ID: {}, isActive: {}", id, isActive);
        return ResponseEntity.ok(ApiResponse.success("User activation updated successfully", updatedUser));
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<AppUserDTO>> getMe() {
        log.debug("Entering getAllUsers");
        AppUserDTO user = userService.getMe();
        log.info("Returning user");
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/users/activate-role/{roleId}")
    public ResponseEntity<ApiResponse<AppUserDTO>> updateCurrentUserRoleActivation(@PathVariable  Long roleId){
        log.debug("Entering updateCurrentUserRoleActivation with roleId: {}", roleId);
        AppUserDTO updatedUser = userService.updateCurrentUserRoleActivation(roleId);
        log.info("User activation updated successfully with the role id: {}", roleId);
        return ResponseEntity.ok(ApiResponse.success("User Role activation updated successfully", updatedUser));
    }

    @PostMapping("/sync-local-users-to-keycloak")
    public ResponseEntity<String> syncLocalUsersToKeycloak() {
        log.info("Received request to sync local users to Keycloak.");

        boolean success = userService.loadUsersFromDatabaseToKeycloak();

        if (success) {
            return new ResponseEntity<>("Users successfully synced to Keycloak.", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("No users were synced. Either no eligible users found or all already exist in Keycloak.", HttpStatus.OK);
        }
    }

    @PostMapping("/delete-keycloak-users")
    public ResponseEntity<String> deleteKeycloakUsers() {
        log.info("Received request to sync local users to Keycloak.");

        boolean success = userService.deleteKeycloakUsers();

        if (success) {
            return new ResponseEntity<>("Users successfully synced to Keycloak.", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("No users were synced. Either no eligible users found or all already exist in Keycloak.", HttpStatus.OK);
        }
    }

    @PostMapping("/delete-keycloak-users-not-in-db")
    public ResponseEntity<String> deleteKeycloakUsersNotInDb() {
        log.info("Received request to sync local users to Keycloak.");

        boolean success = userService.deleteKeycloakUsersNotInDb();

        if (success) {
            return new ResponseEntity<>("Users successfully synced to Keycloak.", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("No users were synced. Either no eligible users found or all already exist in Keycloak.", HttpStatus.OK);
        }
    }
}