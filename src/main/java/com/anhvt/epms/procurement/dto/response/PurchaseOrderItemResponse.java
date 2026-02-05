package com.anhvt.epms.procurement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for Purchase Order Item (line item)
 * Contains item details with material information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemResponse {
    
    private UUID id;
    
    // Material information (nested)
    private MaterialInfo material;
    
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal netAmount; // Calculated: quantity * unitPrice
    private Integer lineNumber;
    private String notes;
    
    /**
     * Nested DTO for Material basic info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaterialInfo {
        private UUID id;
        private String materialCode;
        private String materialName;
        private String description;
        private String unit;
        private String category;
    }
}
