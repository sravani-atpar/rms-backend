package com.RMS_Backend.Restaurant.Management.System.controller;

import com.RMS_Backend.Restaurant.Management.System.dto.ApiResponse;
import com.RMS_Backend.Restaurant.Management.System.dto.RoleDTO;
import com.RMS_Backend.Restaurant.Management.System.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    // Create a new role
    @PostMapping
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@RequestBody RoleDTO role) {
        log.debug("Entering createRole with role: {}", role);
        RoleDTO createdRole = roleService.create(role);
        log.info("Role created successfully with ID: {}", createdRole.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Role created successfully", createdRole));
    }

    // Get all roles
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        log.debug("Entering getAllRoles");
        List<RoleDTO> roles = roleService.getAll();
        if (roles.isEmpty()) {
            log.warn("No roles found");
        }
        log.info("Returning {} roles", roles.size());
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    // Get a role by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(@PathVariable Long id) {
        log.debug("Entering getRoleById with ID: {}", id);
        RoleDTO role = roleService.getById(id);
        if (role == null) {
            log.warn("Role not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Role not found"));
        }
        log.info("Returning role with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role));
    }

    // Update a role
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(@PathVariable Long id, @RequestBody RoleDTO role) {
        log.debug("Entering updateRole with ID: {}, role: {}", id, role);
        role.setId(id);
        RoleDTO updatedRole = roleService.update(id, role);
        if (updatedRole == null) {
            log.warn("Role not found for update with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Role not found"));
        }
        log.info("Role updated successfully with ID: {}", updatedRole.getId());
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updatedRole));
    }

    // Delete a role
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRole(@PathVariable Long id) {
        log.debug("Entering deleteRole with ID: {}", id);
        roleService.delete(id);
        log.info("Role deleted successfully with ID: {}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("Role deleted successfully"));
    }
}