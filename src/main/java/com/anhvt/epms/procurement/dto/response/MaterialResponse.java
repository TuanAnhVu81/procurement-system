package com.anhvt.epms.procurement.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import com.anhvt.epms.procurement.enums.MaterialType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Material response DTO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialResponse {
    
    UUID id;
    
    String materialCode;
    
    String description;
    
    BigDecimal basePrice;
    
    String currency;
    
    String unit;
    
    String category;
    
    MaterialType materialType;
    String imageUrl;

    String manufacturer;
    
    String specifications;
    
    Boolean isActive;
    
    // Audit fields
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
    
    String createdBy;
    
    String updatedBy;
}
