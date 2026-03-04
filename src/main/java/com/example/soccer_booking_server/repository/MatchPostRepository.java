package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.MatchPost;
import com.example.soccer_booking_server.entitis.Booking;
import com.example.soccer_booking_server.enums.MatchPostStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchPostRepository extends JpaRepository<MatchPost, Long> {

    boolean existsByBookingAndStatus(Booking booking, MatchPostStatus status);

    List<MatchPost> findByBooking_BookingId(Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select mp from MatchPost mp where mp.id = :id")
    Optional<MatchPost> findByIdForUpdate(@Param("id") Long id);

    boolean existsByBooking_BookingIdAndStatus(Long bookingId, MatchPostStatus status);

    @Query("""
    select 
      mp.id,
      mp.status,
      mp.note,

      o.userId,
      o.fullName,
      o.avatarUrl,

      b.bookingId,
      f.fieldId,
      f.fieldName,
      f.type,
      f.description,
      b.bookingDate,
      s.slotStart,
      s.slotEnd,
      b.fieldPrice,

      mu.userId,
      mu.fullName,

      coalesce(sum(case when ma.status = com.example.soccer_booking_server.enums.MatchApplicationStatus.PENDING then 1 else 0 end), 0)
    from MatchPost mp
      join mp.owner o
      join mp.booking b
      join b.field f
      join b.slot s
      left join mp.matchedUser mu
      left join MatchApplication ma on ma.post = mp
    group by 
      mp.id, mp.status, mp.note,
      o.userId, o.fullName, o.avatarUrl,
      b.bookingId, f.fieldId, f.fieldName, f.type, f.description, b.bookingDate, s.slotStart, s.slotEnd, b.fieldPrice,
      mu.userId, mu.fullName
    order by mp.createdAt desc
""")
    List<Object[]> findAllForList();
}