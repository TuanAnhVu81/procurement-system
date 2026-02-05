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
                    .map(PurchaseOrder::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            
            // Get currency from first PO or default to USD
            String currency = posWithStatus.isEmpty() ? "USD" : posWithStatus.get(0).getCurrency();
            
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
        
        // Get all approved purchase orders
        List<PurchaseOrder> approvedPOs = purchaseOrderRepository.findByStatus(POStatus.APPROVED);
        
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
                    .map(PurchaseOrder::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String currency = vendorPOs.isEmpty() ? "USD" : vendorPOs.get(0).getCurrency();
            
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
        
        // Group by year-month
        Map<String, List<PurchaseOrder>> groupedByMonth = allPOs.stream()
                .collect(Collectors.groupingBy(po -> 
                    po.getOrderDate().getYear() + "-" + String.format("%02d", po.getOrderDate().getMonthValue())
                ));
        
        // Build monthly trend responses
        List<MonthlyPurchaseTrendResponse> trends = new ArrayList<>();
        
        for (Map.Entry<String, List<PurchaseOrder>> entry : groupedByMonth.entrySet()) {
            String yearMonth = entry.getKey();
            List<PurchaseOrder> monthPOs = entry.getValue();
            
            // Parse year and month
            String[] parts = yearMonth.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            
            // Get month name
            LocalDate sampleDate = LocalDate.of(year, month, 1);
            String monthName = sampleDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            
            // Calculate statistics
            long totalOrders = monthPOs.size();
            long approvedOrders = monthPOs.stream()
                    .filter(po -> po.getStatus() == POStatus.APPROVED)
                    .count();
            
            BigDecimal totalValue = monthPOs.stream()
                    .filter(po -> po.getStatus() == POStatus.APPROVED)
                    .map(PurchaseOrder::getGrandTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal averageOrderValue = approvedOrders > 0 
                    ? totalValue.divide(BigDecimal.valueOf(approvedOrders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            double approvalRate = totalOrders > 0 
                    ? (approvedOrders * 100.0 / totalOrders)
                    : 0.0;
            
            String currency = monthPOs.isEmpty() ? "USD" : monthPOs.get(0).getCurrency();
            
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
        
        // Sort by year and month
        trends.sort(Comparator.comparing(MonthlyPurchaseTrendResponse::getYear)
                .thenComparing(MonthlyPurchaseTrendResponse::getMonth));
        
        log.info("Generated monthly trend with {} months", trends.size());
        return trends;
    }
    
    /**
     * Get monthly purchase trend for the last N months
     */
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPurchaseTrendResponse> getRecentMonthlyTrend(int months) {
        log.info("Generating recent monthly trend for last {} months", months);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        return getMonthlyPurchaseTrend(startDate, endDate);
    }
}
