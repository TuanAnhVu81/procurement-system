package com.anhvt.epms.procurement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Material Type (SAP Code: MTART)
 * Defines the type of material for inventory management and accounting.
 */
public enum MaterialType {
    ROH("Nguyên liệu thô"),
    HALB("Bán thành phẩm"),
    FERT("Thành phẩm"),
    HAWA("Hàng hóa thương mại"),
    DIEN("Dịch vụ"),
    NLAG("Vật tư tiêu hao");

    private final String displayName;

    MaterialType(String displayName) {
        this.displayName = displayName;
    }

    // Returns the human-readable label for API response display
    public String getDisplayName() {
        return displayName;
    }

    // Jackson uses enum name (e.g., "HAWA") for serialization/deserialization
    // to keep it consistent with DB storage (EnumType.STRING)
    @JsonValue
    public String toJson() {
        return this.name();
    }

    // Allow Jackson to deserialize from enum name string (e.g., "HAWA")
    @JsonCreator
    public static MaterialType fromJson(String value) {
        if (value == null) return null;
        for (MaterialType mt : values()) {
            if (mt.name().equalsIgnoreCase(value)) return mt;
        }
        throw new IllegalArgumentException("Unknown MaterialType: " + value);
    }
}
