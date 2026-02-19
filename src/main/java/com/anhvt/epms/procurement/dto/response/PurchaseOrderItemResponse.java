package com.anhvt.epms.procurement.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for Purchase Order Item (line item)
 * Contains item details with material snapshot information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderItemResponse {

    UUID id;

    // Material reference (live entity link)
    MaterialInfo material;

    // Snapshot fields: Frozen at PO creation time for audit trail
    // Preserved even if master data (Material) is updated later
    String materialCode;
    String materialDescription;
    String unit;

    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal netAmount;  // Calculated: quantity * unitPrice

    // Tax calculation at line item level (each item may have different tax rate)
    BigDecimal taxRate;
    BigDecimal taxAmount;  // Calculated: netAmount * taxRate
    BigDecimal lineTotal;  // Calculated: netAmount + taxAmount

    Integer lineNumber;
    String notes;

    /**
     * Nested DTO for Material basic info (reference only)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MaterialInfo {
        UUID id;
        String materialCode;
        String materialName;
        String description;
        String unit;
        String category;
    }
}
