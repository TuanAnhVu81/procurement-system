package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.ChangePasswordRequest;
import com.anhvt.epms.procurement.dto.request.UpdateProfileRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;

/**
 * Service interface for self-service profile management
 */
public interface ProfileService {

    UserResponse getProfileByUsername(String username);

    UserResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
