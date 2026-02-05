package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.dto.response.MonthlyPurchaseTrendResponse;
import com.anhvt.epms.procurement.dto.response.POStatusSummaryResponse;
import com.anhvt.epms.procurement.dto.response.TopVendorResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Analytics and Reporting
 * Provides business intelligence and statistical data for purchase orders
 */
public interface AnalyticsService {
    
    /**
     * Get purchase order status summary
     * Returns aggregated statistics grouped by PO status
     * 
     * Example use case: Dashboard showing PO distribution by status
     * 
     * @return List of status summaries with count and total amount
     */
    List<POStatusSummaryResponse> getPOStatusSummary();
    
    /**
     * Get top vendors by purchase value
     * Returns vendors ranked by total approved purchase order value
     * 
     * Example use case: Identify strategic vendors for negotiation
     * 
     * @param limit Maximum number of vendors to return (default: 5)
     * @return List of top vendors with purchase statistics
     */
    List<TopVendorResponse> getTopVendorsByPurchaseValue(int limit);
    
    /**
     * Get monthly purchase trend
     * Returns purchase statistics aggregated by month
     * 
     * Example use case: Trend analysis for budget planning
     * 
     * @param startDate Start date for the analysis period
     * @param endDate End date for the analysis period
     * @return List of monthly statistics
     */
    List<MonthlyPurchaseTrendResponse> getMonthlyPurchaseTrend(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get monthly purchase trend for the last N months
     * Convenience method for recent trend analysis
     * 
     * @param months Number of months to analyze (e.g., 6 for last 6 months)
     * @return List of monthly statistics
     */
    List<MonthlyPurchaseTrendResponse> getRecentMonthlyTrend(int months);
}
