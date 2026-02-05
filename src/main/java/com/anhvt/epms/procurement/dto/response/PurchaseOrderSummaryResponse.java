package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.POStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Summary Response DTO for Purchase Order (compact version for lists)
 * Excludes nested items for better performance in list views
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderSummaryResponse {
    
    private UUID id;
    private String poNumber;
    
    // Vendor basic info (not fully nested)
    private UUID vendorId;
    private String vendorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;
    
    private POStatus status;
    private String statusDisplay;
    
    // Financial summary
    private BigDecimal grandTotal;
    private String currency;
    
    // Item count instead of full items
    private Integer itemCount;
    
    // Approver info
    private String approverName;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate approvedDate;
    
    // Audit
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}
