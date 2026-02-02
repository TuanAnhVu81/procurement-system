package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.LoginRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.AuthenticationResponse;
import com.anhvt.epms.procurement.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Login endpoint
     * @param request login request containing username and password
     * @return API response with JWT token
     */
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authenticationService.login(request);
        
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Login successful")
                .result(response)
                .build();
    }
}
