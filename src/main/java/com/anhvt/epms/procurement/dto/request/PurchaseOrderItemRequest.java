package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for Purchase Order Item (line item)
 * Contains material, quantity, and pricing information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemRequest {
    
    @NotNull(message = "Material ID is required")
    private UUID materialId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;
    
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    
    private Integer lineNumber; // Optional, for ordering items
}
