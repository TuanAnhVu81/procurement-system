package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.response.MonthlyPurchaseTrendResponse;
import com.anhvt.epms.procurement.dto.response.POStatusSummaryResponse;
import com.anhvt.epms.procurement.dto.response.TopVendorResponse;
import com.anhvt.epms.procurement.entity.PurchaseOrder;
import com.anhvt.epms.procurement.entity.Vendor;
import com.anhvt.epms.procurement.enums.POStatus;
import com.anhvt.epms.procurement.repository.PurchaseOrderRepository;
import com.anhvt.epms.procurement.repository.VendorRepository;
import com.anhvt.epms.procurement.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Analytics and Reporting
 * Provides business intelligence through aggregated purchase order data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;
    
    // Exchange rates for dashboard normalization (simplified for analytics purposes)
    private static final BigDecimal USD_TO_VND = new BigDecimal("25400"); // Approx 1 USD = ~25,400 VND
    private static final BigDecimal EUR_TO_VND = new BigDecimal("27500");

    /**
     * Helper to normalize values to a base currency (VND) for aggregation
     */
    private BigDecimal normalizeToVND(BigDecimal amount, String currency) {
        if (amount == null) return BigDecimal.ZERO;
        if (currency == null) return amount;
        
        return switch (currency.toUpperCase()) {
            case "USD" -> amount.multiply(USD_TO_VND);
            case "EUR" -> amount.multiply(EUR_TO_VND);
            case "VND" -> amount;
            default -> amount; // Fallback
        };
    }

    /**
     * Get purchase order status summary
     * Aggregates PO count and total value by status
     */
    @Override
    @Transactional(readOnly = true)
    public List<POStatusSummaryResponse> getPOStatusSummary() {
        log.info("Generating PO status summary");
        
        // Get all active purchase orders
        List<PurchaseOrder> allPOs = purchaseOrderRepository.findAll();
        
        // Calculate total count for percentage calculation
        long totalCount = allPOs.size();
        
        // Group by status and aggregate
        Map<POStatus, List<PurchaseOrder>> groupedByStatus = allPOs.stream()
                .collect(Collectors.groupingBy(PurchaseOrder::getStatus));
        
        // Build summary responses
        List<POStatusSummaryResponse> summaries = new ArrayList<>();
        
        for (POStatus status : POStatus.values()) {
            List<PurchaseOrder> posWithStatus = groupedByStatus.getOrDefault(status, Collections.emptyList());
            
            long count = posWithStatus.size();
            BigDecimal totalAmount = posWithStatus.stream()
                    .map(po -> normalizeToVND(po.getGrandTotal(), po.getCurrency()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.HALF_UP);
            
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            
            // Fixed base currency after normalization
            String currency = "VND";
            
            POStatusSummaryResponse summary = POStatusSummaryResponse.builder()
                    .status(status.name())
                    .statusDisplay(status.getDisplayName())
                    .count(count)
                    .totalAmount(totalAmount)
                    .currency(currency)
                    .percentage(Math.round(percentage * 100.0) / 100.0) // Round to 2 decimal places
                    .build();
            
            summaries.add(summary);
        }
        
        // Sort by count descending
        summaries.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        
        log.info("Generated status summary for {} statuses", summaries.size());
        return summaries;
    }
    
    /**
     * Get top vendors by purchase value
     * Ranks vendors by total approved PO value
     */
    @Override
    @Transactional(readOnly = true)
    public List<TopVendorResponse> getTopVendorsByPurchaseValue(int limit) {
        log.info("Generating top {} vendors by purchase value", limit);
        
        // Get all approved or received purchase orders
        List<PurchaseOrder> approvedPOs = purchaseOrderRepository.findAll().stream()
                .filter(po -> po.getStatus() == POStatus.APPROVED || po.getStatus() == POStatus.RECEIVED)
                .collect(Collectors.toList());
        
        // Group by vendor and calculate totals
        Map<Vendor, List<PurchaseOrder>> groupedByVendor = approvedPOs.stream()
                .collect(Collectors.groupingBy(PurchaseOrder::getVendor));
        
        // Build vendor statistics
        List<TopVendorResponse> vendorStats = new ArrayList<>();
        
        for (Map.Entry<Vendor, List<PurchaseOrder>> entry : groupedByVendor.entrySet()) {
            Vendor vendor = entry.getKey();
            List<PurchaseOrder> vendorPOs = entry.getValue();
            
            long totalOrders = vendorPOs.size();
            BigDecimal totalValue = vendorPOs.stream()
                    .map(po -> normalizeToVND(po.getGrandTotal(), po.getCurrency()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.HALF_UP);
            
            String currency = "VND"; // All metrics normalized to VND
            
            TopVendorResponse vendorStat = TopVendorResponse.builder()
                    .vendorId(vendor.getId())
                    .vendorCode(vendor.getVendorCode())
                    .vendorName(vendor.getName())
                    .totalOrders(totalOrders)
                    .totalPurchaseValue(totalValue)
                    .currency(currency)
                    .rating(vendor.getRating())
                    .build();
            
            vendorStats.add(vendorStat);
        }
        
        // Sort by total purchase value descending
        vendorStats.sort((a, b) -> b.getTotalPurchaseValue().compareTo(a.getTotalPurchaseValue()));
        
        // Assign ranks and limit results
        int rank = 1;
        List<TopVendorResponse> topVendors = vendorStats.stream()
                .limit(limit)
                .peek(v -> v.setRank(rank))
                .collect(Collectors.toList());
        
        // Update ranks properly
        for (int i = 0; i < topVendors.size(); i++) {
            topVendors.get(i).setRank(i + 1);
        }
        
        log.info("Generated top vendors list with {} entries", topVendors.size());
        return topVendors;
    }
    
    /**
     * Get monthly purchase trend for a date range
     */
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPurchaseTrendResponse> getMonthlyPurchaseTrend(LocalDate startDate, LocalDate endDate) {
        log.info("Generating monthly purchase trend from {} to {}", startDate, endDate);
        
        // Get all POs within date range
        List<PurchaseOrder> allPOs = purchaseOrderRepository.findAll().stream()
                .filter(po -> !po.getOrderDate().isBefore(startDate) && !po.getOrderDate().isAfter(endDate))
                .collect(Collectors.toList());
        
        // Group by specific Date (YYYY-MM-DD)
        Map<String, List<PurchaseOrder>> groupedByDate = allPOs.stream()
                .collect(Collectors.groupingBy(po -> po.getOrderDate().toString()));
        
        // Build monthly trend responses
        List<MonthlyPurchaseTrendResponse> trends = new ArrayList<>();
        
        for (Map.Entry<String, List<PurchaseOrder>> entry : groupedByDate.entrySet()) {
            String dateStr = entry.getKey();
            List<PurchaseOrder> monthPOs = entry.getValue();
            
            // Parse date elements
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            
            // Use monthName field to store 'DD/MM' display value
            String monthName = String.format("%02d/%02d", day, month);
            
            // Calculate statistics
            long totalOrders = monthPOs.size();
            long approvedOrders = monthPOs.stream()
                    .filter(po -> po.getStatus() == POStatus.APPROVED || po.getStatus() == POStatus.RECEIVED)
                    .count();
            
            BigDecimal totalValue = monthPOs.stream()
                    .filter(po -> po.getStatus() == POStatus.APPROVED || po.getStatus() == POStatus.RECEIVED)
                    .map(po -> normalizeToVND(po.getGrandTotal(), po.getCurrency()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(0, RoundingMode.HALF_UP);
            
            BigDecimal averageOrderValue = approvedOrders > 0 
                    ? totalValue.divide(BigDecimal.valueOf(approvedOrders), 0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            double approvalRate = totalOrders > 0 
                    ? (approvedOrders * 100.0 / totalOrders)
                    : 0.0;
            
            String currency = "VND"; // Normalized base currency
            
            MonthlyPurchaseTrendResponse trend = MonthlyPurchaseTrendResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(monthName)
                    .totalOrders(totalOrders)
                    .approvedOrders(approvedOrders)
                    .totalValue(totalValue)
                    .currency(currency)
                    .averageOrderValue(averageOrderValue)
                    .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                    .build();
            
            trends.add(trend);
        }
        
        // Sort chronologically (Year -> Month -> Day)
        trends.sort(Comparator.comparing(MonthlyPurchaseTrendResponse::getYear)
                .thenComparing(MonthlyPurchaseTrendResponse::getMonth)
                .thenComparing(t -> Integer.parseInt(t.getMonthName().split("/")[0])));
        
        log.info("Generated monthly trend with {} months", trends.size());
        return trends;
    }
    
    /**
     * Get monthly purchase trend for the last N months
     */
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPurchaseTrendResponse> getRecentMonthlyTrend(int days) {
        log.info("Generating recent daily trend for last {} days", days);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14); // default to 14 days instead of months
        
        return getMonthlyPurchaseTrend(startDate, endDate);
    }
}
