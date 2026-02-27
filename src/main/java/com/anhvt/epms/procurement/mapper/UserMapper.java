package com.anhvt.epms.procurement.mapper;

import com.anhvt.epms.procurement.dto.request.CreateUserRequest;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.entity.Role;
import com.anhvt.epms.procurement.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity and DTOs
 * Handles conversion between Entity and DTO objects
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper {
    
    /**
     * Convert CreateUserRequest to User entity (Admin-only flow)
     * Password will be encoded separately in the service layer
     * @param request admin user creation request
     * @return User entity
     */
    @Mapping(target = "password", ignore = true) // Will be set to default Welcome@123 after encoding
    @Mapping(target = "status", ignore = true) // Will be set to ACTIVE by default
    @Mapping(target = "roles", ignore = true) // Will be assigned after resolving role names to entities
    @Mapping(target = "requirePasswordChange", ignore = true) // Will be set to true by service
    User toEntity(CreateUserRequest request);
    
    /**
     * Convert User entity to UserResponse DTO
     * @param user user entity
     * @return UserResponse DTO
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserResponse toResponse(User user);
    
    /**
     * Convert Set of Role entities to Set of role names
     * @param roles set of roles
     * @return set of role names
     */
    @Named("rolesToRoleNames")
    default Set<String> rolesToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
