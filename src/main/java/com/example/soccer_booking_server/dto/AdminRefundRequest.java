package com.example.soccer_booking_server.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRefundRequest {
    // số tiền hoàn (nếu null => hoàn = paidAmount hoặc theo rule bạn muốn)
    private BigDecimal amount;
    private String reason;
}
