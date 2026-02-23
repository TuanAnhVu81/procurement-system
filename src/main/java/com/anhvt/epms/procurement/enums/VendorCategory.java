package com.anhvt.epms.procurement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Vendor Category (Account Group in SAP)
 * Defines the type of vendor for reporting and processing purposes.
 */
public enum VendorCategory {
    DOMESTIC("Nhà cung cấp trong nước"),
    FOREIGN("Nhà cung cấp nước ngoài"),
    ONE_TIME("Nhà cung cấp vãng lai"),
    SERVICE("Nhà cung cấp dịch vụ");

    private final String displayName;

    VendorCategory(String displayName) {
        this.displayName = displayName;
    }

    // Returns the human-readable label for API response display
    public String getDisplayName() {
        return displayName;
    }

    // Jackson uses enum name (e.g., "DOMESTIC") for serialization/deserialization
    // to keep it consistent with DB storage (EnumType.STRING)
    @JsonValue
    public String toJson() {
        return this.name();
    }

    // Allow Jackson to deserialize from both enum name string (e.g., "DOMESTIC")
    @JsonCreator
    public static VendorCategory fromJson(String value) {
        if (value == null) return null;
        for (VendorCategory vc : values()) {
            if (vc.name().equalsIgnoreCase(value)) return vc;
        }
        throw new IllegalArgumentException("Unknown VendorCategory: " + value);
    }
}
