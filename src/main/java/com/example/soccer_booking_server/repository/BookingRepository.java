package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.Booking;
import com.example.soccer_booking_server.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {

    @Query("""
        select b.slot.slotId
        from Booking b
        where b.field.fieldId = :fieldId
          and b.bookingDate = :date
          and (
            b.status = com.example.soccer_booking_server.enums.BookingStatus.DEPOSITED
            or b.status = com.example.soccer_booking_server.enums.BookingStatus.CHECKED_IN
            or (b.status = com.example.soccer_booking_server.enums.BookingStatus.PENDING_DEPOSIT
                and (b.depositDueAt is null or b.depositDueAt > :now))
          )
    """)
    List<Integer> findOccupiedSlotIds(
            @Param("fieldId") Integer fieldId,
            @Param("date") LocalDate date,
            @Param("now") LocalDateTime now
    );

    @Query("""
        select (count(b) > 0)
        from Booking b
        where b.field.fieldId = :fieldId
          and b.slot.slotId = :slotId
          and b.bookingDate = :date
          and (
            b.status = com.example.soccer_booking_server.enums.BookingStatus.DEPOSITED
            or b.status = com.example.soccer_booking_server.enums.BookingStatus.CHECKED_IN
            or (b.status = com.example.soccer_booking_server.enums.BookingStatus.PENDING_DEPOSIT
                and (b.depositDueAt is null or b.depositDueAt > :now))
          )
    """)
    boolean existsActiveBooking(
            @Param("fieldId") Integer fieldId,
            @Param("slotId") Integer slotId,
            @Param("date") LocalDate date,
            @Param("now") LocalDateTime now
    );

    List<Booking> findByStatusAndDepositDueAtBefore(BookingStatus status, LocalDateTime now);

    List<Booking> findByStatusAndCheckinDueAtBeforeAndCheckedInAtIsNull(BookingStatus status, LocalDateTime now);

    Page<Booking> findByUser_UserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Page<Booking> findByUser_UserIdAndStatusInOrderByCreatedAtDesc(
            Integer userId,
            Collection<BookingStatus> statuses,
            Pageable pageable
    );

    @Query("""
        select distinct b
        from Booking b
        join fetch b.user u
        join fetch b.field f
        join fetch b.slot s
        left join fetch b.services bs
        left join fetch bs.service sv
        where b.bookingId = :id
    """)
    Optional<Booking> findDetailById(@Param("id") Long id);

    @Query("""
        select b
        from Booking b
        join fetch b.user u
        join fetch b.field f
        join fetch b.slot s
        where (:fieldId is null or f.fieldId = :fieldId)
          and b.bookingDate = :date
        order by s.slotStart asc
    """)
    List<Booking> findScheduleByDate(
            @Param("fieldId") Integer fieldId,
            @Param("date") LocalDate date
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.bookingId = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);
}
