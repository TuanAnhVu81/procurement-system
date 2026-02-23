package com.anhvt.epms.procurement.mapper;

import com.anhvt.epms.procurement.dto.request.VendorRequest;
import com.anhvt.epms.procurement.dto.response.VendorResponse;
import com.anhvt.epms.procurement.entity.Vendor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for Vendor entity and DTOs
 * Handles conversion between Entity and DTO objects
 */
@Mapper(componentModel = "spring")
public interface VendorMapper extends BaseMapper {
    
    /**
     * Convert VendorRequest to Vendor entity
     * VendorCode, Status, and Rating will be set separately in service layer
     * @param request vendor request DTO
     * @return Vendor entity
     */
    @Mapping(target = "vendorCode", ignore = true)  // Auto-generated in service, cannot be set by user
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "ratingComment", ignore = true) // Handled via updateRating API
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "purchaseOrders", ignore = true)
    @Mapping(target = "bankAccountNumber", source = "bankAccountNumber") // Explicit: field name differs from DB column name
    Vendor toEntity(VendorRequest request);
    
    /**
     * Convert Vendor entity to VendorResponse DTO
     * @param vendor vendor entity
     * @return VendorResponse DTO
     */
    @Mapping(target = "categoryDisplay", expression = "java(vendor.getCategory() != null ? vendor.getCategory().getDisplayName() : null)")
    VendorResponse toResponse(Vendor vendor);
    
    /**
     * Convert list of Vendor entities to list of VendorResponse DTOs
     * @param vendors list of vendor entities
     * @return list of VendorResponse DTOs
     */
    List<VendorResponse> toResponseList(List<Vendor> vendors);
    
    /**
     * Update existing Vendor entity from VendorRequest
     * Used for PUT operations to update only the changed fields
     * @param vendor target vendor entity to update
     * @param request source vendor request DTO
     */
    @Mapping(target = "vendorCode", ignore = true) // Vendor code cannot be changed
    @Mapping(target = "rating", ignore = true) // Rating updated via separate endpoint
    @Mapping(target = "ratingComment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "purchaseOrders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(@MappingTarget Vendor vendor, VendorRequest request);
}
