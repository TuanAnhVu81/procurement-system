package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for approving or rejecting a Purchase Order
 * Contains optional comment and rejection reason
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderApprovalRequest {
    
    @Size(max = 500, message = "Approver comment cannot exceed 500 characters")
    private String approverComment;
    
    @Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
    private String rejectionReason; // Only used for rejection
}
