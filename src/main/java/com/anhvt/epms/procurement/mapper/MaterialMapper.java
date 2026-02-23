package com.anhvt.epms.procurement.mapper;

import com.anhvt.epms.procurement.dto.request.MaterialRequest;
import com.anhvt.epms.procurement.dto.response.MaterialResponse;
import com.anhvt.epms.procurement.entity.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for Material entity and DTOs
 * Handles conversion between Entity and DTO objects
 */
@Mapper(componentModel = "spring")
public interface MaterialMapper extends BaseMapper {
    
    /**
     * Convert MaterialRequest to Material entity
     * MaterialCode and isActive will be set separately in service layer
     * @param request material request DTO
     * @return Material entity
     */
    @Mapping(target = "materialCode", ignore = true) // Auto-generated in service layer, user input ignored
    @Mapping(target = "isActive", ignore = true)    // Always set to true on create by service
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "purchaseOrderItems", ignore = true)
    Material toEntity(MaterialRequest request);
    
    /**
     * Convert Material entity to MaterialResponse DTO
     * @param material material entity
     * @return MaterialResponse DTO
     */
    @Mapping(target = "materialTypeDisplay", expression = "java(material.getMaterialType() != null ? material.getMaterialType().getDisplayName() : null)")
    MaterialResponse toResponse(Material material);
    
    /**
     * Convert list of Material entities to list of MaterialResponse DTOs
     * @param materials list of material entities
     * @return list of MaterialResponse DTOs
     */
    List<MaterialResponse> toResponseList(List<Material> materials);
    
    /**
     * Update existing Material entity from MaterialRequest
     * Used for PUT operations to update only the changed fields
     * @param material target material entity to update
     * @param request source material request DTO
     */
    @Mapping(target = "materialCode", ignore = true) // Material code cannot be changed
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "purchaseOrderItems", ignore = true)
    @Mapping(target = "createdAt", ignore  = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(@MappingTarget Material material, MaterialRequest request);
}
