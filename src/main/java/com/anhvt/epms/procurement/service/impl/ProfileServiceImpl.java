package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.ChangePasswordRequest;
import com.anhvt.epms.procurement.dto.request.UpdateProfileRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.mapper.UserMapper;
import com.anhvt.epms.procurement.repository.UserRepository;
import com.anhvt.epms.procurement.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ProfileService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setFullName(request.getFullName());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 1. Verify old password matches
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 2. Validate new password matches confirm pattern
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            // Throw appropriate error, using bad request or custom password mismatch error
            // For now, we will just use a generic exception or create one if we know it exists.
            // But let's check ErrorCode just in case
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 3. Encode new password and clear the required flag
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRequirePasswordChange(false); // Turn off the flag!
        
        userRepository.save(user);
    }
}
