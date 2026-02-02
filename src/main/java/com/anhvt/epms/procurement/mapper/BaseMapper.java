package com.anhvt.epms.procurement.mapper;

/**
 * Marker interface for all mappers
 * Mappers are responsible for converting between Entity and DTO objects
 * 
 * Best practices:
 * - Use MapStruct for automatic mapping generation
 * - Keep mapping logic separate from business logic
 * - Define clear mapping methods for Entity -> DTO and DTO -> Entity
 * 
 * Example usage with MapStruct:
 * 
 * @Mapper(componentModel = "spring")
 * public interface UserMapper extends BaseMapper {
 *     UserResponse toResponse(User user);
 *     User toEntity(UserCreateRequest request);
 *     void updateEntity(@MappingTarget User user, UserUpdateRequest request);
 * }
 */
public interface BaseMapper {
    // Marker interface - no methods needed
    // All specific mappers will extend this interface
}
