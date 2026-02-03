package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.dto.BookingHistoryItemDTO;
import com.example.soccer_booking_server.dto.CreateBookingRequest;
import com.example.soccer_booking_server.dto.CreateBookingResponse;
import com.example.soccer_booking_server.entitis.*;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.util.Set;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingAppService {

    private final BookingRepository bookingRepo;
    private final UserRepository userRepo;
    private final FieldRepository fieldRepo;
    private final FieldSlotRepository slotRepo;
    private final FieldServiceRepository serviceRepo;

    // cọc = 30% tiền sân (KHÔNG tính dịch vụ)
    private static final BigDecimal DEPOSIT_RATE = new BigDecimal("0.30");

    @Transactional
    public CreateBookingResponse createBooking(CreateBookingRequest req) {
        LocalDateTime now = LocalDateTime.now();

        if (req.getUserId() == null || req.getFieldId() == null || req.getSlotId() == null || req.getBookingDate() == null) {
            throw new RuntimeException("Thiếu dữ liệu tạo booking");
        }

        Users user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Field field = fieldRepo.findById(req.getFieldId())
                .orElseThrow(() -> new RuntimeException("Field not found"));

        FieldSlot slot = slotRepo.findById(req.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // 1) check trùng slot (active)
        boolean occupied = bookingRepo.existsActiveBooking(field.getFieldId(), slot.getSlotId(), req.getBookingDate(), now);
        if (occupied) throw new RuntimeException("Khung giờ đã được đặt");

        // 2) tính tiền
        BigDecimal fieldPrice = slot.getPrice(); // snapshot
        if (fieldPrice == null) fieldPrice = BigDecimal.ZERO;

        BigDecimal depositAmount = fieldPrice.multiply(DEPOSIT_RATE).setScale(0, RoundingMode.HALF_UP);

        // ✅ CHẶN NGAY: không đủ coin thì KHÔNG tạo booking
        BigDecimal balance = user.getCoinBalance() == null ? BigDecimal.ZERO : user.getCoinBalance();
        if (balance.compareTo(depositAmount) < 0) {
            throw new RuntimeException("Không đủ xu để đặt cọc. Vui lòng nạp thêm.");
        }

        List<Integer> serviceIds = req.getServiceIds() == null ? List.of() : req.getServiceIds();
        List<FieldServiceEntity> services = serviceIds.isEmpty() ? List.of() : serviceRepo.findAllById(serviceIds);
        if (services.size() != serviceIds.size()) throw new RuntimeException("Có dịch vụ không tồn tại");

        BigDecimal serviceTotal = services.stream()
                .map(FieldServiceEntity::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // coupon: demo (bạn sẽ làm DB coupon sau)
        BigDecimal discount = BigDecimal.ZERO;

        BigDecimal total = fieldPrice.add(serviceTotal).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        // 3) deadlines
        LocalDateTime depositDueAt = now.plusMinutes(20);

        // checkinDueAt = giờ bắt đầu slot + 30p
        LocalDateTime slotStart = LocalDateTime.of(req.getBookingDate(), slot.getSlotStart());
        LocalDateTime checkinDueAt = slotStart.plusMinutes(30);

        Booking booking = Booking.builder()
                .user(user)
                .field(field)
                .slot(slot)
                .bookingDate(req.getBookingDate())
                .status(BookingStatus.PENDING_DEPOSIT)
                .depositDueAt(depositDueAt)
                .checkinDueAt(checkinDueAt)
                .checkedInAt(null)
                .expireReason(null)
                .fieldPrice(fieldPrice)
                .serviceTotal(serviceTotal)
                .depositAmount(depositAmount)
                .paidAmount(BigDecimal.ZERO)
                .totalPrice(total)
                .build();

        // 4) attach services snapshot
        for (FieldServiceEntity s : services) {
            BigDecimal unit = s.getPrice() == null ? BigDecimal.ZERO : s.getPrice();
            int qty = 1;
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));

            com.example.soccer_booking_server.entitis.BookingService bs =
                    com.example.soccer_booking_server.entitis.BookingService.builder()
                            .booking(booking)
                            .service(s)
                            .quantity(qty)
                            .unitPrice(unit)
                            .lineTotal(lineTotal)
                            .build();

            booking.getServices().add(bs);
        }

        Booking saved = bookingRepo.save(booking);

        return new CreateBookingResponse(
                saved.getBookingId(),
                saved.getStatus(),
                saved.getFieldPrice(),
                saved.getServiceTotal(),
                saved.getDepositAmount(),
                saved.getTotalPrice(),
                saved.getDepositDueAt(),
                saved.getCheckinDueAt()
        );
    }


    @Transactional
    public void deposit(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        LocalDateTime now = LocalDateTime.now();

        if (b.getStatus() != BookingStatus.PENDING_DEPOSIT) {
            throw new RuntimeException("Booking không ở trạng thái chờ cọc");
        }

        if (b.getDepositDueAt() != null && b.getDepositDueAt().isBefore(now)) {
            b.setStatus(BookingStatus.EXPIRED);
            b.setExpireReason("DEPOSIT");
            bookingRepo.save(b);
            throw new RuntimeException("Quá hạn cọc");
        }

        Users u = b.getUser();
        BigDecimal need = b.getDepositAmount();

        if (u.getCoinBalance().compareTo(need) < 0) {
            throw new RuntimeException("Không đủ coin để cọc");
        }

        // trừ coin
        u.setCoinBalance(u.getCoinBalance().subtract(need));

        // update booking
        b.setPaidAmount(b.getPaidAmount().add(need));
        b.setStatus(BookingStatus.DEPOSITED);

        bookingRepo.save(b);
    }

    @Transactional
    public void checkIn(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        LocalDateTime now = LocalDateTime.now();

        if (b.getStatus() != BookingStatus.DEPOSITED) {
            throw new RuntimeException("Chỉ check-in khi đã cọc");
        }

        if (b.getCheckinDueAt() != null && b.getCheckinDueAt().isBefore(now)) {
            b.setStatus(BookingStatus.EXPIRED);
            b.setExpireReason("CHECKIN");
            bookingRepo.save(b);
            throw new RuntimeException("Quá hạn check-in");
        }

        b.setCheckedInAt(now);
        b.setStatus(BookingStatus.CHECKED_IN);
        bookingRepo.save(b);
    }

    @Transactional
    public void cancel(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.EXPIRED) return;

        b.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(b);

        // hoàn cọc hay không => bạn quyết rule sau
    }

    @Transactional(readOnly = true)
    public List<Integer> getOccupiedSlots(Integer fieldId, LocalDate date) {
        return bookingRepo.findOccupiedSlotIds(fieldId, date, LocalDateTime.now());
    }


    @Transactional(readOnly = true)
    public Page<BookingHistoryItemDTO> getUserHistory(
            Integer userId,
            Integer page,
            Integer size,
            List<BookingStatus> statuses
    ) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Booking> p;
        if (statuses == null || statuses.isEmpty()) {
            p = bookingRepo.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            p = bookingRepo.findByUser_UserIdAndStatusInOrderByCreatedAtDesc(userId, Set.copyOf(statuses), pageable);
        }

        return p.map(this::toHistoryDto);
    }

    private BookingHistoryItemDTO toHistoryDto(Booking b) {
        return BookingHistoryItemDTO.builder()
                .bookingId(b.getBookingId())
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
                .createdAt(b.getCreatedAt())
                .build();
    }
}
