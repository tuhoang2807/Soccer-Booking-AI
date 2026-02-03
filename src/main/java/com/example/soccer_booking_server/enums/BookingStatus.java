package com.example.soccer_booking_server.enums;

public enum BookingStatus {
    PENDING_DEPOSIT, // tạo đơn, giữ chỗ chờ cọc (20 phút)
    DEPOSITED,       // đã cọc (giữ slot chắc chắn)
    CHECKED_IN,      // đã đến sân xác nhận (trước hạn 30 phút)
    COMPLETED,       // đá xong / kết thúc
    CANCELLED,       // huỷ bởi user/admin
    EXPIRED          // tự huỷ do quá hạn (cọc hoặc check-in)
}

