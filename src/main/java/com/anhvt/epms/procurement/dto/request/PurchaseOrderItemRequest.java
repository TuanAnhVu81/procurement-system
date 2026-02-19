package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for Purchase Order Item (line item)
 * Contains material, quantity, and pricing information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderItemRequest {

    @NotNull(message = "Material ID is required")
    UUID materialId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Tax rate must be at least 0%")
    @DecimalMax(value = "1.0", message = "Tax rate cannot exceed 100%")
    BigDecimal taxRate; // Optional, defaults to 0.10 (10%) if not provided

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes;

    Integer lineNumber; // Optional, for ordering items
}
