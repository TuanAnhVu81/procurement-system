package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.request.LoginRequest;
import com.anhvt.epms.procurement.dto.response.AuthenticationResponse;

/**
 * Service interface for authentication operations
 */
public interface AuthenticationService {
    
    /**
     * Authenticate user and generate JWT token
     * @param request login request containing username and password
     * @return authentication response with JWT token
     */
    AuthenticationResponse login(LoginRequest request);
}
