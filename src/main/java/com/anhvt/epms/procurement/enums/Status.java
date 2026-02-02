package com.anhvt.epms.procurement.enums;

import lombok.Getter;

/**
 * Enum for User/Vendor status
 */
@Getter
public enum Status {
    ACTIVE("Active", "Entity is active"),
    INACTIVE("Inactive", "Entity is inactive"),
    BLOCKED("Blocked", "Entity is blocked");
    
    private final String displayName;
    private final String description;
    
    Status(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
