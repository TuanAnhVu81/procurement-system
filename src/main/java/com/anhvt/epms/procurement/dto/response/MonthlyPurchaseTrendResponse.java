package com.anhvt.epms.procurement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Monthly Purchase Trend
 * Shows purchase statistics aggregated by month
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPurchaseTrendResponse {
    
    /**
     * Year (e.g., 2026)
     */
    private Integer year;
    
    /**
     * Month (1-12)
     */
    private Integer month;
    
    /**
     * Month name (e.g., "January", "February")
     */
    private String monthName;
    
    /**
     * Total number of POs created in this month
     */
    private Long totalOrders;
    
    /**
     * Total number of approved POs in this month
     */
    private Long approvedOrders;
    
    /**
     * Total purchase value (sum of approved PO grand totals)
     */
    private BigDecimal totalValue;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Average order value
     */
    private BigDecimal averageOrderValue;
    
    /**
     * Approval rate (percentage of approved POs)
     */
    private Double approvalRate;
}
