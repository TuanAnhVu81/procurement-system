package com.anhvt.epms.procurement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for returning material stock information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialStockResponse {

    UUID id;

    UUID materialId;

    String materialCode;

    String materialDescription;

    /** Total quantity currently physically in warehouse */
    Integer quantityOnHand;

    /** Quantity reserved for in-progress Purchase Orders (reserved but not yet received) */
    Integer reservedQuantity;

    /** Available = quantityOnHand - reservedQuantity */
    Integer availableQuantity;

    Integer minimumStockLevel;

    Integer maximumStockLevel;

    String warehouseLocation;

    /** Whether the current stock has fallen below the minimum threshold */
    boolean lowStock;

    LocalDateTime lastUpdated;
}
