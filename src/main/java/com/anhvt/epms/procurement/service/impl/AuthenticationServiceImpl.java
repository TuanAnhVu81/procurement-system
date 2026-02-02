package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.LoginRequest;
import com.anhvt.epms.procurement.dto.response.AuthenticationResponse;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.repository.UserRepository;
import com.anhvt.epms.procurement.service.AuthenticationService;
import com.anhvt.epms.procurement.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthenticationService
 * Handles user authentication and JWT token generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Authenticate user and generate JWT token
     * @param request login request containing username and password
     * @return authentication response with JWT token
     */
    @Override
    public AuthenticationResponse login(LoginRequest request) {
        try {
            // Authenticate user with username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Load user from database
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Generate JWT token
            String token = jwtService.generateToken(user);

            log.info("User '{}' logged in successfully", request.getUsername());

            // Return authentication response
            return AuthenticationResponse.builder()
                    .token(token)
                    .expiresIn(jwtExpiration)
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user '{}'", request.getUsername());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
