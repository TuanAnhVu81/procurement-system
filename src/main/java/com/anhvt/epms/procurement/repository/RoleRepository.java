package com.anhvt.epms.procurement.repository;

import com.anhvt.epms.procurement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity
 * Provides database operations for role management
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    /**
     * Find role by name
     * @param name role name (e.g., "ROLE_EMPLOYEE", "ROLE_MANAGER", "ROLE_ADMIN")
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role exists by name
     * @param name role name
     * @return true if role exists, false otherwise
     */
    boolean existsByName(String name);
}
