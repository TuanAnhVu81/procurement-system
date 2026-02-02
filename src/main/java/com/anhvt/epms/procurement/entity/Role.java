package com.anhvt.epms.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity for role-based access control
 * Defines user roles such as ROLE_EMPLOYEE and ROLE_MANAGER
 */
@Entity
@Table(name = "roles",
    indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends BaseEntity {
    
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    String name;
    
    @Column(name = "description", length = 255)
    String description;
    
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    Set<User> users = new HashSet<>();
}
