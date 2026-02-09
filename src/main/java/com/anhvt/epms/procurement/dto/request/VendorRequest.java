package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating and updating Vendor
 * Used for both POST /api/vendors and PUT /api/vendors/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VendorRequest {
    
    @Size(max = 20, message = "Vendor code must not exceed 20 characters")
    String vendorCode;
    
    @NotBlank(message = "Vendor name is required")
    @Size(max = 200, message = "Vendor name must not exceed 200 characters")
    String name;
    
    @Email(message = "Invalid email format")
    String email;
    
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone must be 10-11 digits only")
    String phone;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;
    
    @NotBlank(message = "Tax ID is required")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    String taxId;
    
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    String contactPerson;
    
    @Size(max = 100, message = "Payment terms must not exceed 100 characters")
    String paymentTerms;
    
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    String bankName;

    @Size(max = 50, message = "Bank account number must not exceed 50 characters")
    String bankAccountNumber;

    @Size(max = 100, message = "Bank branch must not exceed 100 characters")
    String bankBranch;

    // Use string to simplify binding, validate later or assume valid
    // Or better, use specific Enum validation if using proper Enum deserializer
    // For now, let's stick to String input in request and allow mapper/enum conversion
    // But since it's an enum, we should probably take it as String or Enum.
    // Let's use the enum directly as Jackson handles it.
    com.anhvt.epms.procurement.enums.VendorCategory category;

    String notes;
}
