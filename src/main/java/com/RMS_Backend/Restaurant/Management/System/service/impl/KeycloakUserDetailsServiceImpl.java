package com.RMS_Backend.Restaurant.Management.System.service.impl;


import com.RMS_Backend.Restaurant.Management.System.keycloak.KeycloakAdminClient;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeycloakUserDetailsServiceImpl implements UserDetailsService {

    private final KeycloakAdminClient keycloakAdminClient;

    public KeycloakUserDetailsServiceImpl(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @Override
    public UserDetails loadUserByUsername(String keycloakId) throws UsernameNotFoundException {
        // Fetch the user by username (Keycloak ID)
        UserResource userResource  = keycloakAdminClient.getUserResource(keycloakId);

        // Assuming only one user is returned
        UserRepresentation userRepresentation = userResource.toRepresentation();
        String userId = userRepresentation.getId();

        // Fetch roles assigned to the user
        List<RoleRepresentation> roles = keycloakAdminClient.getUserResource(userId).roles().realmLevel().listAll();

        // Convert Keycloak roles to Spring Security authorities
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (RoleRepresentation role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        // Create and return a UserDetails object (Spring Security User)
        return new User(keycloakId, "", authorities);
    }
}