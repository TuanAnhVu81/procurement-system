package com.anhvt.epms.procurement.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1012, "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1008, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1009, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1010, "Token has expired", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND(1011, "Role not found", HttpStatus.NOT_FOUND),
    
    // Vendor Management error codes (2000-2099)
    VENDOR_NOT_FOUND(2001, "Vendor not found", HttpStatus.NOT_FOUND),
    VENDOR_CODE_EXISTED(2002, "Vendor code already exists", HttpStatus.BAD_REQUEST),
    INVALID_VENDOR_RATING(2003, "Rating must be between 1.0 and 5.0", HttpStatus.BAD_REQUEST),
    VENDOR_TAX_ID_EXISTED(2004, "Tax ID already exists", HttpStatus.BAD_REQUEST),
    
    // Material Management error codes (2100-2199)
    MATERIAL_NOT_FOUND(2101, "Material not found", HttpStatus.NOT_FOUND),
    MATERIAL_CODE_EXISTED(2102, "Material code already exists", HttpStatus.BAD_REQUEST),
    INVALID_MATERIAL_PRICE(2103, "Base price must be greater than 0", HttpStatus.BAD_REQUEST),
    INVALID_CURRENCY(2104, "Currency must be USD, VND, or EUR", HttpStatus.BAD_REQUEST),
    MATERIAL_STOCK_NOT_FOUND(2105, "Material stock record not found", HttpStatus.NOT_FOUND),

    // Purchase Order error codes (3000-3099)
    PURCHASE_ORDER_NOT_FOUND(3001, "Purchase order not found", HttpStatus.NOT_FOUND),
    PURCHASE_ORDER_INVALID_STATUS(3002, "Operation not permitted for current PO status", HttpStatus.BAD_REQUEST),
    PURCHASE_ORDER_ACCESS_DENIED(3003, "You are not allowed to perform this action on this purchase order", HttpStatus.FORBIDDEN),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
