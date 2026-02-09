package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Request DTO for creating and updating Material
 * Used for both POST /api/materials and PUT /api/materials/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialRequest {
    
    @Size(max = 50, message = "Material code must not exceed 50 characters")
    String materialCode;
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description;
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Base price format is invalid (max 13 integers, 2 decimals)")
    BigDecimal basePrice;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "USD|VND|EUR", message = "Currency must be USD, VND, or EUR")
    @Builder.Default
    String currency = "USD";
    
    @NotBlank(message = "Unit is required")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @Builder.Default
    String unit = "PCS";
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    String category;
    
    @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
    String manufacturer;
    
    String specifications;
}
