package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.RegisterRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.mapper.UserMapper;
import com.anhvt.epms.procurement.repository.UserRepository;
import com.anhvt.epms.procurement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService
 * Handles user management operations including registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     * @param request registration request
     * @return created user response
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Map DTO to Entity using MapStruct
        User user = userMapper.toEntity(request);
        
        // Set additional fields
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(Status.ACTIVE);
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        log.info("User '{}' registered successfully", savedUser.getUsername());
        
        // Map Entity to Response DTO using MapStruct
        return userMapper.toResponse(savedUser);
    }
}
