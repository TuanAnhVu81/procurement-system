package com.anhvt.epms.procurement.dto.response;

import com.anhvt.epms.procurement.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for user response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    
    UUID id;
    
    String username;
    
    String email;
    
    String fullName;
    
    Status status;
    
    Set<String> roles;
    
    boolean requirePasswordChange;
    
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
}
