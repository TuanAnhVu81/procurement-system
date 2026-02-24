package com.anhvt.epms.procurement.controller;

import com.anhvt.epms.procurement.dto.request.CreateUserRequest;
import com.anhvt.epms.procurement.dto.request.UpdateRoleRequest;
import com.anhvt.epms.procurement.dto.request.UpdateUserRequest;
import com.anhvt.epms.procurement.dto.response.ApiResponse;
import com.anhvt.epms.procurement.dto.response.UserResponse;
import com.anhvt.epms.procurement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Administration", description = "Admin-only APIs for managing user lifecycles and roles")
public class UserAdminController {

    private final UserService userService;

    @Operation(summary = "Get all users (with pagination and filters)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<UserResponse> users = userService.getAllUsers(role, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Retrieved all users successfully"));
    }

    @Operation(summary = "Get user details by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id), "Retrieved user successfully"));
    }

    @Operation(summary = "Create a new employee account")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createEmployeeAccount(@Valid @RequestBody CreateUserRequest request) {
        UserResponse newUser = userService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newUser, "User created successfully with default password"));
    }

    @Operation(summary = "Update employee information")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateEmployeeInformation(
            @PathVariable UUID id, 
            @Valid @RequestBody UpdateUserRequest request) {
        
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @Operation(summary = "Promote or demote employee (Change Role)")
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable UUID id, 
            @Valid @RequestBody UpdateRoleRequest request) {
        
        userService.changeUserRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success(null, "User role updated successfully"));
    }

    @Operation(summary = "Deactivate or reactivate an account (Soft Delete)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable UUID id, 
            @RequestParam boolean isActive) {
        
        userService.toggleUserStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(null, "User status toggled successfully"));
    }

    @Operation(summary = "Reset password to default")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable UUID id) {
        userService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User password reset. Require password change is activated."));
    }
}
