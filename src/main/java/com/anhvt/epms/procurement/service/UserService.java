package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.CreateUserRequest;
import com.anhvt.epms.procurement.dto.request.RegisterRequest;
import com.anhvt.epms.procurement.dto.request.UpdateUserRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

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

    /**
     * Get all users with optional filtering and pagination
     * @param role User role name to filter by
     * @param status User status to filter by (true = active, false = inactive)
     * @param pageable Pagination and sorting information
     * @return Page of user responses
     */
    Page<UserResponse> getAllUsers(String role, Boolean status, Pageable pageable);

    /**
     * Get user details by ID
     * @param id User ID
     * @return User response details
     */
    UserResponse getUserById(UUID id);

    /**
     * Create a new employee account (Admin only)
     * @param request creation request with user details
     * @return created user response
     */
    UserResponse createEmployee(CreateUserRequest request);

    /**
     * Update employee information (Admin only)
     * @param id User ID
     * @param request update request details
     * @return updated user response
     */
    UserResponse updateUser(UUID id, UpdateUserRequest request);

    /**
     * Change user's role (Promote/Demote)
     * @param id User ID
     * @param roleName new role name
     */
    void changeUserRole(UUID id, String roleName);

    /**
     * Toggle user's active status (Soft delete / Reactivate)
     * @param id User ID
     * @param isActive target status boolean
     */
    void toggleUserStatus(UUID id, boolean isActive);

    /**
     * Reset user's password to default
     * @param id User ID
     */
    void resetPassword(UUID id);
}
