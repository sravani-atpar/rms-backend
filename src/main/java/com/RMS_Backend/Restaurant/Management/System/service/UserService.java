package com.RMS_Backend.Restaurant.Management.System.service;



import com.RMS_Backend.Restaurant.Management.System.dto.AppUserDTO;
import com.RMS_Backend.Restaurant.Management.System.dto.RegisterRequest;
import com.RMS_Backend.Restaurant.Management.System.model.AppUser;
import com.RMS_Backend.Restaurant.Management.System.model.Role;
import jakarta.transaction.Transactional;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {


    AppUserDTO registerUser(RegisterRequest registerRequest);

    AppUserDTO registerImportedUser(RegisterRequest registerRequest);

    AppUser createUser(RegisterRequest registerRequest, Role role);

    AppUser createImportedUser(RegisterRequest registerRequest, Role role);

    AccessTokenResponse login(String identity, String identityType, String password);

    List<AppUserDTO> getAllUsers();

    Page<AppUserDTO> getPaginatedUsers(int page, int size);

    AppUserDTO getUserById(Long id);

    AppUserDTO updateUser(Long id, AppUserDTO appUserDTO);

    AppUserDTO updateUserActivation(Long id, boolean isActive);

    void deleteUser(Long userId);

    AppUserDTO getUserBykeycloakId(String keycloakId);

    AppUserDTO saveUser(AppUser appUser);

//    AppUserDTO syncUserRoles(Long id, SyncUserRolesDTO syncUserRolesDTO);
//
//    AppUserDTO initialUserActivation(Long id, List<InitialActivateUserDTO> initialActivateUserDTOs, boolean isFromBulkImport);

    List<AppUserDTO> getAllUsersByRole(Long roleId);

    Page<AppUserDTO> getPaginatedUsersByRole(Long roleId, int page, int size);


    AccessTokenResponse getAccessToken(String refreshToken);

    AppUserDTO getMe();




//    List<AppUserDTO> createBulkUsersByRole(BulkUserCreateRequest bulkUserCreateRequest);

    AppUser getAppUserEntityBykeycloakId(String loginKeycloakId);

    AppUser getAppUserEntity(Long id);

    @Transactional
        // Ensure database updates within the loop are handled transactionally
    boolean loadUsersFromDatabaseToKeycloak();

    @Transactional
        // Ensure database updates within the loop are handled transactionally
    boolean deleteKeycloakUsers();

    AppUser save(AppUser updateUser);

    AppUserDTO updateCurrentUserRoleActivation(Long roleId);

    /**
     * Find all users matching the given criteria.
     *
     * @param criteria the criteria to filter users
     * @return a list of users matching the criteria
     */
//    List<AppUserDTO> findAllUsers(UserCriteria criteria);

    /**
     * Find paginated users matching the given criteria.
     *
//     * @param criteria the criteria to filter users
//     * @param pageable the pagination information
     * @return a page of users matching the criteria
     */
//    Page<AppUserDTO> findPaginatedUsers(UserCriteria criteria, Pageable pageable);

    @Transactional
    boolean deleteKeycloakUsersNotInDb();
}

