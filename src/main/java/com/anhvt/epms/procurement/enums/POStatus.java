package com.anhvt.epms.procurement.enums;

import lombok.Getter;

/**
 * Enum for Purchase Order status workflow
 * Represents the lifecycle of a purchase order from creation to completion
 */
@Getter
public enum POStatus {
    CREATED("Created", "Purchase order has been created"),
    PENDING("Pending Approval", "Purchase order is awaiting approval"),
    APPROVED("Approved", "Purchase order has been approved"),
    REJECTED("Rejected", "Purchase order has been rejected"),
    CANCELLED("Cancelled", "Purchase order has been cancelled"),
    RECEIVED("Received", "Goods have been received and stock has been updated");
    
    private final String displayName;
    private final String description;
    
    POStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
