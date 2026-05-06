package com.example.soccer_booking_server.scheduler;

import com.example.soccer_booking_server.entitis.Booking;
import com.example.soccer_booking_server.entitis.MatchPost;
import com.example.soccer_booking_server.enums.MatchPostStatus;
import com.example.soccer_booking_server.repository.MatchPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchPostExpiryScheduler {

    private final MatchPostRepository matchPostRepository;

    private LocalDateTime bookingStartTime(Booking booking) {
        return LocalDateTime.of(
                booking.getBookingDate(),
                booking.getSlot().getSlotStart()
        );
    }

    private LocalDateTime bookingEndTime(Booking booking) {
        return LocalDateTime.of(
                booking.getBookingDate(),
                booking.getSlot().getSlotEnd()
        );
    }

    @Scheduled(fixedDelay = 60000) // chạy mỗi 1 phút
    @Transactional
    public void expireOldPosts() {
        List<MatchPost> posts = matchPostRepository.findAllActivePosts();
        LocalDateTime now = LocalDateTime.now();

        for (MatchPost post : posts) {
            Booking booking = post.getBooking();

            if (post.getStatus() == MatchPostStatus.OPEN) {
                // OPEN hết hạn khi tới giờ bắt đầu
                if (!now.isBefore(bookingStartTime(booking))) {
                    post.setStatus(MatchPostStatus.EXPIRED);
                }
            } else if (post.getStatus() == MatchPostStatus.MATCHED) {
                // MATCHED hết hạn khi qua giờ kết thúc
                if (now.isAfter(bookingEndTime(booking))) {
                    post.setStatus(MatchPostStatus.EXPIRED);
                }
            }
        }
    }
}