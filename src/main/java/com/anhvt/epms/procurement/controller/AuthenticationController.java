package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.LoginRequest;
import com.anhvt.epms.procurement.dto.request.RegisterRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.AuthenticationResponse;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.service.AuthenticationService;
import com.anhvt.epms.procurement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Registration APIs")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Register a new user
     * 
     * IMPORTANT: Public registration only creates ROLE_EMPLOYEE accounts.
     * Admin and Manager accounts are created automatically on app startup via DataInitializer.
     * 
     * Default accounts are configured via environment variables or application.yaml:
     * - app.security.admin.username / password
     * - app.security.manager.username / password
     * 
     * @param request registration request
     * @return API response with user data (role will be ROLE_EMPLOYEE)
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new employee user", 
        description = "Create a new user account with ROLE_EMPLOYEE. Admin and Manager accounts are pre-configured via environment variables."
    )
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        
        return ApiResponse.<UserResponse>builder()

                .message("User registered successfully")
                .result(response)
                .build();
    }

    /**
     * Login endpoint
     * @param request login request containing username and password
     * @return API response with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and generate JWT token")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authenticationService.login(request);
        
        return ApiResponse.<AuthenticationResponse>builder()

                .message("Login successful")
                .result(response)
                .build();
    }
}
