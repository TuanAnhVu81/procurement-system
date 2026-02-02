package com.anhvt.epms.procurement.mapper;

import com.anhvt.epms.procurement.dto.request.RegisterRequest;
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
     * Convert RegisterRequest to User entity
     * Password will be encoded separately in the service layer
     * @param request registration request
     * @return User entity
     */
    @Mapping(target = "password", ignore = true) // Will be set after encoding
    @Mapping(target = "status", ignore = true) // Will be set to ACTIVE by default
    @Mapping(target = "roles", ignore = true) // Will be set separately
    User toEntity(RegisterRequest request);
    
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
