package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.RegisterRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;

/**
 * Service interface for user management operations
 */
public interface UserService {
    
    /**
     * Register a new user
     * @param request registration request
     * @return created user response
     */
    UserResponse register(RegisterRequest request);

    org.springframework.data.domain.Page<UserResponse> getAllUsers(String role, Boolean status, org.springframework.data.domain.Pageable pageable);

    UserResponse getUserById(java.util.UUID id);

    UserResponse createEmployee(com.anhvt.epms.procurement.dto.request.CreateUserRequest request);

    UserResponse updateUser(java.util.UUID id, com.anhvt.epms.procurement.dto.request.UpdateUserRequest request);

    void changeUserRole(java.util.UUID id, String roleName);

    void toggleUserStatus(java.util.UUID id, boolean isActive);

    void resetPassword(java.util.UUID id);
}
