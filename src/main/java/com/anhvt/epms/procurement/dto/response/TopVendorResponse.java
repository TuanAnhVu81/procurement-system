package com.anhvt.epms.procurement.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopVendorResponse {

    /** Vendor ID */
    UUID vendorId;

    /** Vendor code (e.g., VEN-001) */
    String vendorCode;

    /** Vendor name */
    String vendorName;

    /** Total number of purchase orders from this vendor */
    Long totalOrders;

    /** Total purchase value (sum of all approved PO grand totals) */
    BigDecimal totalPurchaseValue;

    /** Currency code */
    String currency;

    /** Vendor rating (1.0 to 5.0) */
    Double rating;

    /** Rank position (1 = highest purchase value) */
    Integer rank;
}
