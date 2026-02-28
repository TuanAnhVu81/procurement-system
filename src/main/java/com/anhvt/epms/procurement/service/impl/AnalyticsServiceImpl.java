package com.anhvt.epms.procurement.service.impl;

import com.anhvt.epms.procurement.dto.response.MonthlyPurchaseTrendResponse;
import com.anhvt.epms.procurement.dto.response.POStatusSummaryResponse;
import com.anhvt.epms.procurement.dto.response.TopVendorResponse;
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
 * Service implementation for Analytics and Reporting.
 *
 * PERFORMANCE: All analytics methods use targeted DB-side aggregation queries
 * (GROUP BY, SUM, COUNT) via JPA @Query instead of loading all rows into memory.
 * This avoids the anti-pattern of `findAll()` followed by in-memory streaming.
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
     * Normalize a monetary amount to VND for cross-currency comparison.
     * Called on pre-aggregated values returned from DB queries.
     */
    private BigDecimal normalizeToVND(BigDecimal amount, String currency) {
        if (amount == null) return BigDecimal.ZERO;
        if (currency == null) return amount;

        return switch (currency.toUpperCase()) {
            case "USD" -> amount.multiply(USD_TO_VND);
            case "EUR" -> amount.multiply(EUR_TO_VND);
            case "VND" -> amount;
            default -> amount; // Fallback: treat as VND
        };
    }

    /**
     * Get purchase order status summary.
     *
     * PERFORMANCE FIX: Replaced findAll() + Java stream grouping with a
     * DB-side GROUP BY query (countPurchaseOrdersByStatus). Only aggregated
     * rows are transferred from DB to app — not individual PO rows.
     */
    @Override
    @Transactional(readOnly = true)
    public List<POStatusSummaryResponse> getPOStatusSummary() {
        log.info("Generating PO status summary via DB aggregation");

        // DB-side aggregation: returns [status, count, totalGrandTotal, currency]
        List<Object[]> rawRows = purchaseOrderRepository.countPurchaseOrdersByStatus();

        // Accumulate per status (may have multiple rows per status if currencies differ)
        Map<POStatus, long[]>   countMap  = new LinkedHashMap<>(); // [0] = count
        Map<POStatus, BigDecimal> totalMap = new LinkedHashMap<>();

        long grandTotalCount = 0;

        for (Object[] row : rawRows) {
            POStatus status   = (POStatus) row[0];
            long     count    = ((Number) row[1]).longValue();
            BigDecimal amount = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            String currency   = row[3] != null ? (String) row[3] : "VND";

            // Normalize currency to VND before accumulating
            BigDecimal vndAmount = normalizeToVND(amount, currency);

            countMap.merge(status, new long[]{count}, (a, b) -> { a[0] += b[0]; return a; });
            totalMap.merge(status, vndAmount, BigDecimal::add);
            grandTotalCount += count;
        }

        final long finalTotalCount = grandTotalCount;

        // Build summary for ALL known statuses (including ones with zero POs)
        List<POStatusSummaryResponse> summaries = new ArrayList<>();
        for (POStatus status : POStatus.values()) {
            long count = countMap.containsKey(status) ? countMap.get(status)[0] : 0L;
            BigDecimal totalAmount = totalMap.getOrDefault(status, BigDecimal.ZERO)
                    .setScale(0, RoundingMode.HALF_UP);

            double percentage = finalTotalCount > 0 ? (count * 100.0 / finalTotalCount) : 0.0;

            summaries.add(POStatusSummaryResponse.builder()
                    .status(status.name())
                    .statusDisplay(status.getDisplayName())
                    .count(count)
                    .totalAmount(totalAmount)
                    .currency("VND")
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }

        // Sort by count descending for dashboard display
        summaries.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));

        log.info("Generated status summary for {} statuses (total {} POs)", summaries.size(), finalTotalCount);
        return summaries;
    }

    /**
     * Get top vendors by purchase value.
     *
     * PERFORMANCE FIX: Replaced findAll() + Java groupingBy(vendor) with a
     * DB-side GROUP BY vendor.id query (getTotalPurchaseAmountByVendor).
     * Currency normalization is still done in-app, but only on N vendor rows,
     * not on potentially thousands of PO rows.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TopVendorResponse> getTopVendorsByPurchaseValue(int limit) {
        log.info("Generating top {} vendors by purchase value via DB aggregation", limit);

        // DB-side aggregation: returns [vendorId, totalGrandTotal, currency]
        List<Object[]> rawRows = purchaseOrderRepository.getTotalPurchaseAmountByVendor();

        // Accumulate per vendor (multiple rows if vendor has POs in different currencies)
        Map<UUID, BigDecimal> vendorTotalMap = new LinkedHashMap<>();
        Map<UUID, Long>       vendorCountMap = new LinkedHashMap<>();

        for (Object[] row : rawRows) {
            UUID       vendorId = (UUID) row[0];
            BigDecimal amount   = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            String     currency = row[2] != null ? (String) row[2] : "VND";

            BigDecimal vndAmount = normalizeToVND(amount, currency);
            vendorTotalMap.merge(vendorId, vndAmount, BigDecimal::add);
            // Count: we count vendors by distinct aggregated rows, not PO count here
            vendorCountMap.merge(vendorId, 1L, Long::sum);
        }

        // Fetch vendor details by IDs (single IN query — no N+1)
        List<UUID> vendorIds = new ArrayList<>(vendorTotalMap.keySet());
        Map<UUID, Vendor> vendorMap = vendorRepository.findAllById(vendorIds)
                .stream()
                .collect(Collectors.toMap(v -> v.getId(), v -> v));

        // Build top vendor responses
        List<TopVendorResponse> vendorStats = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> entry : vendorTotalMap.entrySet()) {
            UUID vendorId = entry.getKey();
            Vendor vendor = vendorMap.get(vendorId);
            if (vendor == null) continue; // Guard: vendor may have been deleted

            BigDecimal totalValue = entry.getValue().setScale(0, RoundingMode.HALF_UP);

            vendorStats.add(TopVendorResponse.builder()
                    .vendorId(vendor.getId())
                    .vendorCode(vendor.getVendorCode())
                    .vendorName(vendor.getName())
                    .totalOrders(vendorCountMap.getOrDefault(vendorId, 0L))
                    .totalPurchaseValue(totalValue)
                    .currency("VND")
                    .rating(vendor.getRating())
                    .build());
        }

        // Sort descending by total value (DB ordering is per-currency slice, not post-normalized)
        vendorStats.sort((a, b) -> b.getTotalPurchaseValue().compareTo(a.getTotalPurchaseValue()));

        // Limit and assign ranks
        List<TopVendorResponse> topVendors = vendorStats.stream().limit(limit).collect(Collectors.toList());
        for (int i = 0; i < topVendors.size(); i++) {
            topVendors.get(i).setRank(i + 1);
        }

        log.info("Generated top {} vendors list", topVendors.size());
        return topVendors;
    }

    /**
     * Get monthly purchase trend for a date range.
     *
     * PERFORMANCE FIX: Replaced findAll() + Java date-filter streaming with a
     * DB-side date-range query using findPurchaseTrendByDateRange (BETWEEN clause),
     * only fetching pre-aggregated rows for the requested window.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPurchaseTrendResponse> getMonthlyPurchaseTrend(LocalDate startDate, LocalDate endDate) {
        log.info("Generating purchase trend from {} to {} via DB aggregation", startDate, endDate);

        // DB-side aggregation: returns [orderDate, count, currency, totalGrandTotal]
        List<Object[]> rawRows = purchaseOrderRepository.findPurchaseTrendByDateRange(startDate, endDate);

        // Accumulate per date (multiple rows per date if currencies differ)
        record DayKey(LocalDate date) {}
        Map<DayKey, Long>       countMap  = new LinkedHashMap<>();
        Map<DayKey, BigDecimal> totalMap  = new LinkedHashMap<>();

        for (Object[] row : rawRows) {
            LocalDate  date     = (LocalDate) row[0];
            long       count    = ((Number) row[1]).longValue();
            String     currency = row[2] != null ? (String) row[2] : "VND";
            BigDecimal amount   = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

            BigDecimal vndAmount = normalizeToVND(amount, currency);
            DayKey key = new DayKey(date);
            countMap.merge(key, count, Long::sum);
            totalMap.merge(key, vndAmount, BigDecimal::add);
        }

        // Build trend responses
        List<MonthlyPurchaseTrendResponse> trends = new ArrayList<>();
        for (Map.Entry<DayKey, Long> entry : countMap.entrySet()) {
            LocalDate date      = entry.getKey().date();
            long totalOrders    = entry.getValue();
            BigDecimal totalValue = totalMap.getOrDefault(entry.getKey(), BigDecimal.ZERO)
                    .setScale(0, RoundingMode.HALF_UP);

            // Note: approvedOrders here uses same count since query already filters by status
            long approvedOrders = totalOrders;

            BigDecimal averageOrderValue = approvedOrders > 0
                    ? totalValue.divide(BigDecimal.valueOf(approvedOrders), 0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            double approvalRate = 100.0; // All rows in query are non-cancelled

            // Display date as DD/MM for charts
            String displayDate = String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());

            trends.add(MonthlyPurchaseTrendResponse.builder()
                    .year(date.getYear())
                    .month(date.getMonthValue())
                    .monthName(displayDate)
                    .totalOrders(totalOrders)
                    .approvedOrders(approvedOrders)
                    .totalValue(totalValue)
                    .currency("VND")
                    .averageOrderValue(averageOrderValue)
                    .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                    .build());
        }

        // Sort chronologically
        trends.sort(Comparator.comparing(MonthlyPurchaseTrendResponse::getYear)
                .thenComparing(MonthlyPurchaseTrendResponse::getMonth)
                .thenComparing(t -> Integer.parseInt(t.getMonthName().split("/")[0])));

        log.info("Generated trend with {} data points", trends.size());
        return trends;
    }

    /**
     * Get purchase trend for the last N days (default 14 days for dashboard).
     */
    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPurchaseTrendResponse> getRecentMonthlyTrend(int days) {
        log.info("Generating recent daily trend for last {} days", days);
        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14); // default 14-day window
        return getMonthlyPurchaseTrend(startDate, endDate);
    }
}
