package com.anhvt.epms.procurement.enums;

/**
 * Vendor Category (Account Group in SAP)
 * Defines the type of vendor for reporting and processing purposes.
 */
public enum VendorCategory {
    DOMESTIC,   // Domestic Vendor (Trong nước)
    FOREIGN,    // Foreign Vendor (Nước ngoài - cần thủ tục nhập khẩu)
    ONE_TIME,   // One-time Vendor (Vãng lai)
    SERVICE     // Service Provider (Cung cấp dịch vụ)
}
