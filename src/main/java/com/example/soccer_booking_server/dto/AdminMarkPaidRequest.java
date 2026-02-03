package com.example.soccer_booking_server.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMarkPaidRequest {
    // số tiền admin muốn trừ thêm từ ví để thanh toán nốt (nếu null => tự tính remaining)
    private BigDecimal amount;
}

