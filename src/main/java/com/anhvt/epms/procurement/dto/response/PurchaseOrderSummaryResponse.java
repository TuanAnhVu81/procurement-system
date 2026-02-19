package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.POStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Summary Response DTO for Purchase Order (compact version for list views)
 * Excludes nested line items for better performance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderSummaryResponse {

    UUID id;
    String poNumber;

    // Vendor basic info (flattened — no full nested object for summary)
    UUID vendorId;
    String vendorName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate orderDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deliveryDate;

    POStatus status;
    String statusDisplay;

    // Financial summary
    BigDecimal grandTotal;
    String currency;

    // Item count instead of full items list
    Integer itemCount;

    // Approver info
    String approverName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate approvedDate;

    // Audit
    String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createdAt;
}
