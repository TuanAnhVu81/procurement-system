package com.anhvt.epms.procurement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for Top Vendor by Purchase Value
 * Shows vendors ranked by total purchase amount
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopVendorResponse {
    
    /**
     * Vendor ID
     */
    private UUID vendorId;
    
    /**
     * Vendor code (e.g., VEN-001)
     */
    private String vendorCode;
    
    /**
     * Vendor name
     */
    private String vendorName;
    
    /**
     * Total number of purchase orders from this vendor
     */
    private Long totalOrders;
    
    /**
     * Total purchase value (sum of all approved PO grand totals)
     */
    private BigDecimal totalPurchaseValue;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Vendor rating (1.0 to 5.0)
     */
    private Double rating;
    
    /**
     * Rank position (1 = highest purchase value)
     */
    private Integer rank;
}
