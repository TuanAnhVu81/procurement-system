package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.POStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Purchase Order with full details
 * Includes nested items, vendor info, and approver details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderResponse {

    UUID id;
    String poNumber;

    // Vendor information (nested)
    VendorInfo vendor;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate orderDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deliveryDate;

    String deliveryAddress;

    POStatus status;
    String statusDisplay; // Human-readable status label

    // Financial information (aggregated from line items)
    BigDecimal totalAmount;
    BigDecimal taxAmount;
    BigDecimal grandTotal;
    String currency;

    // Line items (full nested list)
    List<PurchaseOrderItemResponse> items;

    // Approval information
    ApproverInfo approver;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate approvedDate;

    String rejectionReason;
    String notes;

    // Audit fields
    String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    String modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime modifiedAt;

    /**
     * Nested DTO for Vendor basic info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VendorInfo {
        UUID id;
        String vendorCode;
        String vendorName;
        String contactPerson;
        String email;
        String phone;
    }

    /**
     * Nested DTO for Approver basic info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ApproverInfo {
        UUID id;
        String username;
        String fullName;
        String email;
    }
}
