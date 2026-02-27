package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.LoginRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.AuthenticationResponse;
import com.anhvt.epms.procurement.service.AuthenticationService;
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
