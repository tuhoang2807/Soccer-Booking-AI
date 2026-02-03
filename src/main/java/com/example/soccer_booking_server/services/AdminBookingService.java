package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.dto.*;
import com.example.soccer_booking_server.entitis.*;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.repository.BookingRepository;
import com.example.soccer_booking_server.repository.UserRepository;
import com.example.soccer_booking_server.repository.BookingSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public Page<AdminBookingListItemDTO> list(
            Integer page, Integer size,
            Integer fieldId, Integer slotId, Integer userId,
            LocalDate from, LocalDate to,
            List<BookingStatus> statuses,
            String q
    ) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var spec = BookingSpecifications.withFilters(fieldId, slotId, userId, from, to, statuses, q);
        Page<Booking> p = bookingRepo.findAll(spec, pageable);

        return p.map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public AdminBookingDetailDTO detail(Long id) {
        Booking b = bookingRepo.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return toDetail(b);
    }

    @Transactional
    public void markPaid(Long id, AdminMarkPaidRequest req) {
        Booking b = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.EXPIRED) {
            throw new RuntimeException("Không thể thanh toán cho booking đã huỷ/expired");
        }

        BigDecimal total = nz(b.getTotalPrice());
        BigDecimal paid = nz(b.getPaidAmount());
        BigDecimal remaining = total.subtract(paid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            // đã đủ tiền rồi -> set completed nếu bạn muốn
            if (b.getStatus() != BookingStatus.COMPLETED) {
                b.setStatus(BookingStatus.COMPLETED);
                bookingRepo.save(b);
            }
            return;
        }

        BigDecimal amount = (req != null && req.getAmount() != null) ? req.getAmount() : remaining;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Amount không hợp lệ");
        if (amount.compareTo(remaining) > 0) amount = remaining;

        Users u = b.getUser();
        BigDecimal balance = nz(u.getCoinBalance());
        if (balance.compareTo(amount) < 0) throw new RuntimeException("User không đủ coin để thanh toán");

        // trừ coin + cập nhật paid
        u.setCoinBalance(balance.subtract(amount));
        b.setPaidAmount(paid.add(amount));

        // đủ tiền -> COMPLETED
        BigDecimal newPaid = b.getPaidAmount();
        if (newPaid.compareTo(total) >= 0) {
            b.setStatus(BookingStatus.COMPLETED);
        }

        bookingRepo.save(b);
    }


    @Transactional
    public void refund(Long id, AdminRefundRequest req) {
        Booking b = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Users u = b.getUser();

        BigDecimal paid = nz(b.getPaidAmount());
        if (paid.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal amount = (req != null && req.getAmount() != null) ? req.getAmount() : paid;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException("Amount không hợp lệ");
        if (amount.compareTo(paid) > 0) amount = paid;

        // cộng coin lại cho user
        u.setCoinBalance(nz(u.getCoinBalance()).add(amount));

        // giảm paid_amount
        b.setPaidAmount(paid.subtract(amount));

        bookingRepo.save(b);
    }

    @Transactional
    public void forceExpire(Long id, String reason) {
        Booking b = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (b.getStatus() == BookingStatus.CANCELLED) throw new RuntimeException("Booking đã huỷ");
        b.setStatus(BookingStatus.EXPIRED);
        b.setExpireReason(reason == null ? "ADMIN" : reason);
        bookingRepo.save(b);
    }

    @Transactional
    public void updateNote(Long id, AdminNoteRequest req) {
        Booking b = bookingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        b.setAdminNote(req == null ? null : req.getNote());
        bookingRepo.save(b);
    }

    @Transactional(readOnly = true)
    public List<AdminScheduleItemDTO> schedule(Integer fieldId, LocalDate date) {
        List<Booking> rows = bookingRepo.findScheduleByDate(fieldId, date);
        return rows.stream().map(this::toSchedule).toList();
    }

    @Transactional(readOnly = true)
    public AdminBookingStatsDTO stats(LocalDate from, LocalDate to) {
        // làm nhanh: dùng spec + list all rồi aggregate
        // nếu data lớn, bạn nên làm @Query group by
        var spec = BookingSpecifications.withFilters(null, null, null, from, to, null, null);
        List<Booking> all = bookingRepo.findAll(spec);

        long total = all.size();
        long cancelled = all.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        long expired = all.stream().filter(b -> b.getStatus() == BookingStatus.EXPIRED).count();

        long pending = all.stream().filter(b -> b.getStatus() == BookingStatus.PENDING_DEPOSIT).count();
        long deposited = all.stream().filter(b -> b.getStatus() == BookingStatus.DEPOSITED).count();
        long checkedIn = all.stream().filter(b -> b.getStatus() == BookingStatus.CHECKED_IN).count();

        BigDecimal gross = all.stream().map(Booking::getTotalPrice).map(this::nz).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = all.stream().map(Booking::getPaidAmount).map(this::nz).reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminBookingStatsDTO.builder()
                .totalBookings(total)
                .cancelled(cancelled)
                .expired(expired)
                .pendingDeposit(pending)
                .deposited(deposited)
                .checkedIn(checkedIn)
                .grossTotal(gross)
                .paidTotal(paid)
                .build();
    }

    // ================== mapping ==================

    private AdminBookingListItemDTO toListItem(Booking b) {
        return AdminBookingListItemDTO.builder()
                .bookingId(b.getBookingId())
                .userId(b.getUser().getUserId())
                .userFullName(b.getUser().getFullName())
                .userEmail(b.getUser().getEmail())
                .userPhone(b.getUser().getPhone())
                .fieldId(b.getField().getFieldId())
                .fieldName(b.getField().getFieldName())
                .slotId(b.getSlot().getSlotId())
                .slotNumber(b.getSlot().getSlotNumber())
                .slotStart(b.getSlot().getSlotStart())
                .slotEnd(b.getSlot().getSlotEnd())
                .bookingDate(b.getBookingDate())
                .status(b.getStatus())
                .fieldPrice(b.getFieldPrice())
                .serviceTotal(b.getServiceTotal())
                .depositAmount(b.getDepositAmount())
                .paidAmount(b.getPaidAmount())
                .totalPrice(b.getTotalPrice())
                .depositDueAt(b.getDepositDueAt())
                .checkinDueAt(b.getCheckinDueAt())
                .checkedInAt(b.getCheckedInAt())
                .expireReason(b.getExpireReason())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private AdminBookingDetailDTO toDetail(Booking b) {
        var services = b.getServices() == null
                ? List.<AdminBookingDetailDTO.ServiceLineDTO>of()
                : b.getServices().stream().map(bs ->
                AdminBookingDetailDTO.ServiceLineDTO.builder()
                        .serviceId(bs.getService().getServiceId())
                        .serviceName(bs.getService().getServiceName())
                        .quantity(bs.getQuantity())
                        .unitPrice(bs.getUnitPrice())
                        .lineTotal(bs.getLineTotal())
                        .build()
        ).toList();

        return AdminBookingDetailDTO.builder()
                .bookingId(b.getBookingId())
                .userId(b.getUser().getUserId())
                .userFullName(b.getUser().getFullName())
                .userEmail(b.getUser().getEmail())
                .userPhone(b.getUser().getPhone())
                .fieldId(b.getField().getFieldId())
                .fieldName(b.getField().getFieldName())
                .slotId(b.getSlot().getSlotId())
                .slotNumber(b.getSlot().getSlotNumber())
                .slotStart(b.getSlot().getSlotStart())
                .slotEnd(b.getSlot().getSlotEnd())
                .bookingDate(b.getBookingDate())
                .status(b.getStatus())
                .fieldPrice(b.getFieldPrice())
                .serviceTotal(b.getServiceTotal())
                .depositAmount(b.getDepositAmount())
                .paidAmount(b.getPaidAmount())
                .totalPrice(b.getTotalPrice())
                .depositDueAt(b.getDepositDueAt())
                .checkinDueAt(b.getCheckinDueAt())
                .checkedInAt(b.getCheckedInAt())
                .expireReason(b.getExpireReason())
                .adminNote(b.getAdminNote())
                .services(services)
                .createdAt(b.getCreatedAt())
                .build();
    }


    private AdminScheduleItemDTO toSchedule(Booking b) {
        return AdminScheduleItemDTO.builder()
                .bookingId(b.getBookingId())
                .fieldId(b.getField().getFieldId())
                .fieldName(b.getField().getFieldName())
                .slotId(b.getSlot().getSlotId())
                .slotNumber(b.getSlot().getSlotNumber())
                .slotStart(b.getSlot().getSlotStart())
                .slotEnd(b.getSlot().getSlotEnd())
                .bookingDate(b.getBookingDate())
                .status(b.getStatus())
                .userId(b.getUser().getUserId())
                .userFullName(b.getUser().getFullName())
                .userPhone(b.getUser().getPhone())
                .build();
    }

    private BigDecimal nz(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }
}

