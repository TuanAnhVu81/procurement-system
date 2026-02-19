package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating or updating Purchase Order
 * Contains PO header information and line items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderRequest {

    @NotNull(message = "Vendor ID is required")
    UUID vendorId;

    @NotNull(message = "Order date is required")
    @PastOrPresent(message = "Order date cannot be in the future")
    LocalDate orderDate;

    @Future(message = "Delivery date must be in the future")
    LocalDate deliveryDate;

    @Size(max = 500, message = "Delivery address cannot exceed 500 characters")
    String deliveryAddress;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    String currency = "USD";

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    String notes;

    @NotEmpty(message = "Purchase order must have at least one item")
    @Valid
    List<PurchaseOrderItemRequest> items;
}
