package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.POStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class PurchaseOrderResponse {
    
    private UUID id;
    private String poNumber;
    
    // Vendor information (nested)
    private VendorInfo vendor;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;
    
    private String deliveryAddress;
    
    private POStatus status;
    private String statusDisplay; // For human-readable status
    
    // Financial information (aggregated from line items)
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String currency;
    
    // Items (nested)
    private List<PurchaseOrderItemResponse> items;
    
    // Approval information
    private ApproverInfo approver;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate approvedDate;
    
    private String rejectionReason;
    private String notes;
    
    // Audit fields
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private String modifiedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;
    
    /**
     * Nested DTO for Vendor basic info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorInfo {
        private UUID id;
        private String vendorCode;
        private String vendorName;
        private String contactPerson;
        private String email;
        private String phone;
    }
    
    /**
     * Nested DTO for Approver basic info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproverInfo {
        private UUID id;
        private String username;
        private String fullName;
        private String email;
    }
}
