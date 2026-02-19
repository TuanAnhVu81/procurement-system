package com.anhvt.epms.procurement.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Response DTO for Purchase Order Status Summary
 * Provides aggregated statistics grouped by PO status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class POStatusSummaryResponse {

    /** PO Status (CREATED, PENDING, APPROVED, REJECTED, CANCELLED) */
    String status;

    /** Display name for the status */
    String statusDisplay;

    /** Total number of POs with this status */
    Long count;

    /** Total amount (sum of grandTotal) for all POs with this status */
    BigDecimal totalAmount;

    /** Currency code (e.g., USD, VND, EUR) */
    String currency;

    /** Percentage of total POs */
    Double percentage;
}
