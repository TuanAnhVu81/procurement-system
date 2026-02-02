package com.anhvt.epms.procurement.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO for authentication response containing JWT token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    
    String token;
    
    Long expiresIn;
}
