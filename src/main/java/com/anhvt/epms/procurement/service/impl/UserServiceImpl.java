package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.CreateUserRequest;
import com.anhvt.epms.procurement.dto.request.UpdateUserRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.entity.Role;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.mapper.UserMapper;
import com.anhvt.epms.procurement.repository.RoleRepository;
import com.anhvt.epms.procurement.repository.UserRepository;
import com.anhvt.epms.procurement.service.UserService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of UserService
 * Handles user management operations including registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponse> getAllUsers(String role, Boolean status, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null && !role.trim().isEmpty()) {
                Join<User, Role> roleJoin = root.join("roles");
                predicates.add(cb.equal(roleJoin.get("name"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status ? Status.ACTIVE : Status.INACTIVE));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createEmployee(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                // Admin assigns default password, so the user MUST change it
                .password(passwordEncoder.encode("Welcome@123")) 
                .requirePasswordChange(true) // Turn on the flag
                .status(Status.ACTIVE)
                .roles(new HashSet<>())
                .build();

        // Assign Role
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                user.addRole(role);
            }
        } else {
            // Default role is EMPLOYEE
            Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.addRole(role);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                
        // Only update email if it changed and doesn't belong to another user
        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
            user.setEmail(request.getEmail());
        }
        
        user.setFullName(request.getFullName());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changeUserRole(UUID id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                
        // For simplicity, we replace existing roles with the new single role.
        user.getRoles().clear();
        user.addRole(newRole);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(UUID id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(isActive ? Status.ACTIVE : Status.INACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode("Welcome@123"));
        user.setRequirePasswordChange(true);
        userRepository.save(user);
    }
}
