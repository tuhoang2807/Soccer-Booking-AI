package com.example.soccer_booking_server.entitis;

import com.example.soccer_booking_server.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private FieldSlot slot;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING_DEPOSIT;

    // ====== Deadlines / Check-in ======
    // 20 phút chưa cọc => EXPIRED
    @Column(name = "deposit_due_at")
    private LocalDateTime depositDueAt;

    // 30 phút chưa check-in => EXPIRED (tính theo giờ bắt đầu slot hoặc theo lúc tạo đơn)
    @Column(name = "checkin_due_at")
    private LocalDateTime checkinDueAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    // optional: lý do expired (DEPOSIT/CHECKIN)
    @Column(name = "expire_reason", length = 30)
    private String expireReason;

    // ====== Money ======
    // tiền sân (slot.price snapshot)
    @Column(name = "field_price", nullable = false)
    @Builder.Default
    private BigDecimal fieldPrice = BigDecimal.ZERO;

    // tổng dịch vụ (snapshot)
    @Column(name = "service_total", nullable = false)
    @Builder.Default
    private BigDecimal serviceTotal = BigDecimal.ZERO;

    // cọc = 30% tiền sân (không tính dịch vụ)
    @Column(name = "deposit_amount", nullable = false)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    // tổng đã trả (trong ví nội bộ bạn sẽ tăng dần)
    @Column(name = "paid_amount", nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // ====== booking -> booking_service (1-n) ======
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingService> services = new ArrayList<>();

    @Column(name = "admin_note", length = 500)
    private String adminNote;
}
