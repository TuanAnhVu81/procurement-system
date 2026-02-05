package com.anhvt.epms.procurement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Purchase Order Status Summary
 * Provides aggregated statistics grouped by PO status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class POStatusSummaryResponse {
    
    /**
     * PO Status (CREATED, PENDING, APPROVED, REJECTED, CANCELLED)
     */
    private String status;
    
    /**
     * Display name for the status
     */
    private String statusDisplay;
    
    /**
     * Total number of POs with this status
     */
    private Long count;
    
    /**
     * Total amount (sum of grandTotal) for all POs with this status
     */
    private BigDecimal totalAmount;
    
    /**
     * Currency code (e.g., USD, VND, EUR)
     */
    private String currency;
    
    /**
     * Percentage of total POs
     */
    private Double percentage;
}
