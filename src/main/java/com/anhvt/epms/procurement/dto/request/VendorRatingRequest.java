package com.anhvt.epms.procurement.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for updating vendor rating
 * Special endpoint: PUT /api/vendors/{id}/rating
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VendorRatingRequest {
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    Double rating;
    
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    String comment;
}
