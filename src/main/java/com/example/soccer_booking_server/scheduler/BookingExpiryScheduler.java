package com.example.soccer_booking_server.scheduler;

import com.example.soccer_booking_server.entitis.Booking;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepo;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireBookings() {
        LocalDateTime now = LocalDateTime.now();

        // quá 20p chưa cọc
        List<Booking> pending = bookingRepo.findByStatusAndDepositDueAtBefore(BookingStatus.PENDING_DEPOSIT, now);
        for (Booking b : pending) {
            b.setStatus(BookingStatus.EXPIRED);
            b.setExpireReason("DEPOSIT");
        }

        // quá 30p sau giờ bắt đầu mà chưa check-in
        List<Booking> notCheckedIn = bookingRepo.findByStatusAndCheckinDueAtBeforeAndCheckedInAtIsNull(BookingStatus.DEPOSITED, now);
        for (Booking b : notCheckedIn) {
            b.setStatus(BookingStatus.EXPIRED);
            b.setExpireReason("CHECKIN");
        }
    }
}

