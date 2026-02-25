package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.ChangePasswordRequest;
import com.anhvt.epms.procurement.dto.request.UpdateProfileRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;

/**
 * Service interface for self-service profile management
 */
public interface ProfileService {

    /**
     * Get personal profile details by username
     * @param username the currently logged in username
     * @return user profile response
     */
    UserResponse getProfileByUsername(String username);

    /**
     * Update personal profile information
     * @param username the currently logged in username
     * @param request profile update details
     * @return updated user profile response
     */
    UserResponse updateProfile(String username, UpdateProfileRequest request);

    /**
     * Change user's own password
     * @param username the currently logged in username
     * @param request password change request details
     */
    void changePassword(String username, ChangePasswordRequest request);
}
