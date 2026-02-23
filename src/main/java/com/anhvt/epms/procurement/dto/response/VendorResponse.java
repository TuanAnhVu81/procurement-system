package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.VendorCategory;
import com.anhvt.epms.procurement.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vendor response DTO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VendorResponse {

    UUID id;
    String vendorCode;
    String name;
    String email;
    String phone;
    String address;
    String taxId;
    String contactPerson;
    String paymentTerms;

    VendorCategory category;
    String categoryDisplay; // Human-readable label (e.g., "Nhà cung cấp trong nước")
    String bankName;
    String bankAccountNumber;
    String bankBranch;
    Double rating;
    String ratingComment;
    Status status;
    String notes;

    // Audit fields
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String createdBy;
    String updatedBy;
}
