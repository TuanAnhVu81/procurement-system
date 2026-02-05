package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.MonthlyPurchaseTrendResponse;
import com.anhvt.epms.procurement.dto.response.POStatusSummaryResponse;
import com.anhvt.epms.procurement.dto.response.TopVendorResponse;
import com.anhvt.epms.procurement.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Analytics and Reporting
 * Provides business intelligence endpoints for purchase order analytics
 * Phase 3: WITHOUT authorization (@PreAuthorize) - will be added in Phase 4
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Reporting", description = "APIs for business intelligence and statistical reporting")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    /**
     * Get purchase order status summary
     * GET /api/analytics/po-status-summary
     * 
     * Returns aggregated statistics showing:
     * - Count of POs by status
     * - Total amount by status
     * - Percentage distribution
     * 
     * Use case: Dashboard overview showing PO distribution
     */
    @GetMapping("/po-status-summary")
    @Operation(
        summary = "Get PO status summary",
        description = "Returns aggregated statistics of purchase orders grouped by status (CREATED, PENDING, APPROVED, etc.)"
    )
    public ApiResponse<List<POStatusSummaryResponse>> getPOStatusSummary() {
        
        List<POStatusSummaryResponse> summary = analyticsService.getPOStatusSummary();
        
        return ApiResponse.<List<POStatusSummaryResponse>>builder()
                .code(1000)
                .message("PO status summary retrieved successfully")
                .result(summary)
                .build();
    }
    
    /**
     * Get top vendors by purchase value
     * GET /api/analytics/top-vendors
     * 
     * Returns vendors ranked by total approved purchase order value
     * 
     * Use case: Identify strategic vendors for price negotiation
     */
    @GetMapping("/top-vendors")
    @Operation(
        summary = "Get top vendors by purchase value",
        description = "Returns vendors ranked by total approved purchase order value. Useful for identifying strategic vendors."
    )
    public ApiResponse<List<TopVendorResponse>> getTopVendors(
            @Parameter(description = "Maximum number of vendors to return (default: 5)")
            @RequestParam(defaultValue = "5") int limit) {
        
        // Validate limit
        if (limit < 1 || limit > 100) {
            limit = 5; // Default to 5 if invalid
        }
        
        List<TopVendorResponse> topVendors = analyticsService.getTopVendorsByPurchaseValue(limit);
        
        return ApiResponse.<List<TopVendorResponse>>builder()
                .code(1000)
                .message(String.format("Top %d vendors retrieved successfully", topVendors.size()))
                .result(topVendors)
                .build();
    }
    
    /**
     * Get monthly purchase trend
     * GET /api/analytics/monthly-trend
     * 
     * Returns purchase statistics aggregated by month for a date range
     * 
     * Use case: Trend analysis for budget planning and forecasting
     */
    @GetMapping("/monthly-trend")
    @Operation(
        summary = "Get monthly purchase trend",
        description = "Returns purchase statistics aggregated by month for the specified date range"
    )
    public ApiResponse<List<MonthlyPurchaseTrendResponse>> getMonthlyTrend(
            @Parameter(description = "Start date (format: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (format: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            return ApiResponse.<List<MonthlyPurchaseTrendResponse>>builder()
                    .code(4000)
                    .message("Start date must be before end date")
                    .build();
        }
        
        List<MonthlyPurchaseTrendResponse> trend = analyticsService.getMonthlyPurchaseTrend(startDate, endDate);
        
        return ApiResponse.<List<MonthlyPurchaseTrendResponse>>builder()
                .code(1000)
                .message("Monthly purchase trend retrieved successfully")
                .result(trend)
                .build();
    }
    
    /**
     * Get recent monthly purchase trend
     * GET /api/analytics/recent-trend
     * 
     * Convenience endpoint for getting trend for the last N months
     * 
     * Use case: Quick dashboard view of recent purchase activity
     */
    @GetMapping("/recent-trend")
    @Operation(
        summary = "Get recent monthly purchase trend",
        description = "Returns purchase statistics for the last N months (default: 6 months)"
    )
    public ApiResponse<List<MonthlyPurchaseTrendResponse>> getRecentTrend(
            @Parameter(description = "Number of months to analyze (default: 6)")
            @RequestParam(defaultValue = "6") int months) {
        
        // Validate months parameter
        if (months < 1 || months > 24) {
            months = 6; // Default to 6 months if invalid
        }
        
        List<MonthlyPurchaseTrendResponse> trend = analyticsService.getRecentMonthlyTrend(months);
        
        return ApiResponse.<List<MonthlyPurchaseTrendResponse>>builder()
                .code(1000)
                .message(String.format("Recent %d months trend retrieved successfully", months))
                .result(trend)
                .build();
    }
    
    /**
     * Get comprehensive analytics dashboard data
     * GET /api/analytics/dashboard
     * 
     * Returns all key analytics in a single call for dashboard display
     * 
     * Use case: Single API call to populate management dashboard
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Get comprehensive dashboard analytics",
        description = "Returns all key analytics (status summary, top vendors, recent trend) in a single response"
    )
    public ApiResponse<DashboardAnalytics> getDashboardAnalytics() {
        
        // Gather all analytics data
        List<POStatusSummaryResponse> statusSummary = analyticsService.getPOStatusSummary();
        List<TopVendorResponse> topVendors = analyticsService.getTopVendorsByPurchaseValue(5);
        List<MonthlyPurchaseTrendResponse> recentTrend = analyticsService.getRecentMonthlyTrend(6);
        
        DashboardAnalytics dashboard = DashboardAnalytics.builder()
                .statusSummary(statusSummary)
                .topVendors(topVendors)
                .monthlyTrend(recentTrend)
                .generatedAt(LocalDate.now())
                .build();
        
        return ApiResponse.<DashboardAnalytics>builder()
                .code(1000)
                .message("Dashboard analytics retrieved successfully")
                .result(dashboard)
                .build();
    }
    
    /**
     * Inner class for comprehensive dashboard response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardAnalytics {
        private List<POStatusSummaryResponse> statusSummary;
        private List<TopVendorResponse> topVendors;
        private List<MonthlyPurchaseTrendResponse> monthlyTrend;
        private LocalDate generatedAt;
    }
}
