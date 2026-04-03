package demo.ticket_app.entity;

public enum VoucherType {
    PLATFORM,              // Platform-wide sale (admin cấu hình)
    USER_MONTHLY,          // Voucher hàng tháng cấp cho user (tự expire)
    ORGANIZER_EVENT,       // Voucher tổ chức tạo cho event cụ thể
    ORGANIZER_PLATFORM     // Voucher tổ chức tạo dùng chung (platform-wide)
}
