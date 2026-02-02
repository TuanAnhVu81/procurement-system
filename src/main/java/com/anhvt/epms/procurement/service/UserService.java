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
}
