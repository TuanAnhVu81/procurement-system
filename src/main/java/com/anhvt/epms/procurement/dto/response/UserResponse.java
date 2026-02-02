package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for user response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    
    Long id;
    
    String username;
    
    String email;
    
    String fullName;
    
    Status status;
    
    Set<String> roles;
    
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
}
