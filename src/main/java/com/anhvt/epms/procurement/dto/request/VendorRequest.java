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
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    String phone;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;
    
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    String taxId;
    
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    String contactPerson;
    
    @Size(max = 100, message = "Payment terms must not exceed 100 characters")
    String paymentTerms;
    
    String notes;
}
