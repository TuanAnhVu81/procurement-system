package com.anhvt.epms.procurement.enums;

/**
 * Material Type (SAP Code: MTART)
 * Defines the type of material for inventory management and accounting.
 */
public enum MaterialType {
    ROH,    // Raw Materials (Nguyên liệu thô - Mua để sản xuất)
    HALB,   // Semifinished Products (Bán thành phẩm)
    FERT,   // Finished Goods (Thành phẩm - Tự sản xuất để bán)
    HAWA,   // Trading Goods (Hàng hóa thương mại - Mua đi bán lại)
    DIEN,   // Services (Dịch vụ - Không quản lý tồn kho)
    NLAG    // Non-stock Material (Vật tư tiêu hao - Mua dùng ngay)
}
