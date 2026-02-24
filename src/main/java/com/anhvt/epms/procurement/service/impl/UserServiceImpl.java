package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.request.RegisterRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.exception.AppException;
import com.anhvt.epms.procurement.exception.ErrorCode;
import com.anhvt.epms.procurement.mapper.UserMapper;
import com.anhvt.epms.procurement.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     * Public registration only creates ROLE_EMPLOYEE accounts
     * Admin and Manager accounts are created via DataInitializer on app startup
     * 
     * @param request registration request
     * @return created user response
     */
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username '{}' already exists", request.getUsername());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email '{}' already exists", request.getEmail());
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // Map DTO to Entity using MapStruct
        User user = userMapper.toEntity(request);
        
        // Set additional fields
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(Status.ACTIVE);
        
        // Assign ROLE_EMPLOYEE to new user (default role for public registration)
        var employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        
        user.addRole(employeeRole);
        
        // Save user to database
        User savedUser = userRepository.save(user);
        
        log.info("User '{}' registered successfully with role: ROLE_EMPLOYEE", savedUser.getUsername());
        
        // Map Entity to Response DTO using MapStruct
        return userMapper.toResponse(savedUser);
    }
    @Override
    public org.springframework.data.domain.Page<UserResponse> getAllUsers(String role, Boolean status, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<User> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (role != null && !role.trim().isEmpty()) {
                jakarta.persistence.criteria.Join<User, com.anhvt.epms.procurement.entity.Role> roleJoin = root.join("roles");
                predicates.add(cb.equal(roleJoin.get("name"), role));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status ? Status.ACTIVE : Status.INACTIVE));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    @Override
    public UserResponse getUserById(java.util.UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createEmployee(com.anhvt.epms.procurement.dto.request.CreateUserRequest request) {
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
                .roles(new java.util.HashSet<>())
                .build();

        // Assign Role
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                com.anhvt.epms.procurement.entity.Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                user.addRole(role);
            }
        } else {
            // Default role is EMPLOYEE
            com.anhvt.epms.procurement.entity.Role role = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.addRole(role);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(java.util.UUID id, com.anhvt.epms.procurement.dto.request.UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setFullName(request.getFullName());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changeUserRole(java.util.UUID id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                
        com.anhvt.epms.procurement.entity.Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                
        // For simplicity, we replace existing roles with the new single role.
        user.getRoles().clear();
        user.addRole(newRole);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(java.util.UUID id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(isActive ? Status.ACTIVE : Status.INACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(java.util.UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode("Welcome@123"));
        user.setRequirePasswordChange(true);
        userRepository.save(user);
    }
}
