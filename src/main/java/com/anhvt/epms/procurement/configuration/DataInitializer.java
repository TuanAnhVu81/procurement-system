package com.anhvt.epms.procurement.configuration;

import com.anhvt.epms.procurement.entity.Role;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.repository.RoleRepository;
import com.anhvt.epms.procurement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Initializer Component
 * Runs on application startup to initialize default roles and admin/manager accounts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Admin Credentials
    @Value("${app.security.admin.username}")
    private String adminUsername;
    
    @Value("${app.security.admin.password}")
    private String adminPassword;
    
    @Value("${app.security.admin.email}")
    private String adminEmail;

    // Manager Credentials
    @Value("${app.security.manager.username}")
    private String managerUsername;
    
    @Value("${app.security.manager.password}")
    private String managerPassword;
    
    @Value("${app.security.manager.email}")
    private String managerEmail;

    /**
     * Initialize default data on application startup
     * Creates roles and default admin/manager accounts if they don't exist
     */
    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Starting Data Initialization ===");
        
        // Step 1: Initialize Roles
        initializeRoles();
        
        // Step 2: Initialize Admin Accounts
        initializeAdminAccounts();
        
        // Step 3: Initialize Manager Accounts
        initializeManagerAccounts();
        
        log.info("=== Data Initialization Completed ===");
    }

    /**
     * Create default roles if they don't exist
     * Roles: ROLE_ADMIN, ROLE_MANAGER, ROLE_EMPLOYEE
     */
    private void initializeRoles() {
        log.info("Initializing roles...");
        
        createRoleIfNotExists("ROLE_ADMIN", "System Administrator - Manages master data");
        createRoleIfNotExists("ROLE_MANAGER", "Manager - Approves purchase orders and views analytics");
        createRoleIfNotExists("ROLE_EMPLOYEE", "Employee - Creates and manages purchase orders");
        
        log.info("Roles initialization completed");
    }

    /**
     * Create role if it doesn't exist
     */
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        } else {
            log.debug("Role already exists: {}", roleName);
        }
    }

    /**
     * Create default admin accounts
     * Creates admin accounts using credentials from environment variables
     */
    private void initializeAdminAccounts() {
        log.info("Initializing admin accounts...");
        
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
        
        // Admin 1: Primary System Administrator
        createUserIfNotExists(
                adminUsername,
                adminPassword,
                adminEmail,
                "System Administrator",
                adminRole
        );
        
        // Admin 2: Backup System Administrator (for demo purposes)
        createUserIfNotExists(
                adminUsername + "2",
                adminPassword,
                adminEmail.replace("@", "2@"),
                "Backup Administrator",
                adminRole
        );
        
        log.info("Admin accounts initialization completed");
    }

    /**
     * Create default manager accounts
     * Creates manager accounts using credentials from environment variables
     */
    private void initializeManagerAccounts() {
        log.info("Initializing manager accounts...");
        
        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));
        
        // Manager 1: Procurement Manager
        createUserIfNotExists(
                managerUsername,
                managerPassword,
                managerEmail,
                "Procurement Manager",
                managerRole
        );
        
        // Manager 2: Department Manager
        createUserIfNotExists(
                managerUsername + "2",
                managerPassword,
                managerEmail.replace("@", "2@"),
                "Department Manager",
                managerRole
        );
        
        log.info("Manager accounts initialization completed");
    }

    /**
     * Create user if not exists
     * Helper method to create a user with specified role
     */
    private void createUserIfNotExists(
            String username, 
            String password, 
            String email, 
            String fullName, 
            Role role) {
        
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .fullName(fullName)
                    .status(Status.ACTIVE)
                    .build();
            
            // Add role to user
            user.addRole(role);
            
            userRepository.save(user);
            log.info("Created user: {} with role: {}", username, role.getName());
        } else {
            // Check if existing user has the role, if not add it
            User existingUser = userRepository.findByUsername(username).orElseThrow();
            boolean hasRole = existingUser.getRoles().stream()
                    .anyMatch(r -> r.getName().equals(role.getName()));
            
            if (!hasRole) {
                existingUser.addRole(role);
                userRepository.save(existingUser);
                log.info("Added missing role {} to existing user: {}", role.getName(), username);
            } else {
                log.debug("User already exists with correct role: {}", username);
            }
        }
    }
}
