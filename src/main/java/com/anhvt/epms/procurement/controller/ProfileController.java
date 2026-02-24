package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.ChangePasswordRequest;
import com.anhvt.epms.procurement.dto.request.UpdateProfileRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "My Profile", description = "Self-service APIs for the currently logged-in user")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get logged-in user's profile")
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Principal principal) {
        String username = principal.getName();
        UserResponse profile = profileService.getProfileByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(profile, "Retrieved profile successfully"));
    }

    @Operation(summary = "Update personal information")
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request, 
            Principal principal) {
        
        String username = principal.getName();
        UserResponse updatedProfile = profileService.updateProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile, "Profile updated successfully"));
    }

    @Operation(summary = "Change password")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request, 
            Principal principal) {
        
        String username = principal.getName();
        profileService.changePassword(username, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
