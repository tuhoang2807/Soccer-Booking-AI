package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.Booking;
import com.example.soccer_booking_server.enums.BookingStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class BookingSpecifications {

    public static Specification<Booking> withFilters(
            Integer fieldId,
            Integer slotId,
            Integer userId,
            LocalDate from,
            LocalDate to,
            List<BookingStatus> statuses,
            String q
    ) {
        return (root, query, cb) -> {
            // fetch joins để giảm N+1 khi list
            // chỉ fetch khi query trả entity (không phải count)
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("user", JoinType.LEFT);
                root.fetch("field", JoinType.LEFT);
                root.fetch("slot", JoinType.LEFT);
                query.distinct(true);
            }

            var predicates = cb.conjunction();

            if (fieldId != null) predicates = cb.and(predicates, cb.equal(root.get("field").get("fieldId"), fieldId));
            if (slotId != null) predicates = cb.and(predicates, cb.equal(root.get("slot").get("slotId"), slotId));
            if (userId != null) predicates = cb.and(predicates, cb.equal(root.get("user").get("userId"), userId));

            if (from != null) predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("bookingDate"), from));
            if (to != null) predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("bookingDate"), to));

            if (statuses != null && !statuses.isEmpty()) {
                predicates = cb.and(predicates, root.get("status").in(statuses));
            }

            if (q != null && !q.trim().isEmpty()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                var uFullName = cb.lower(root.get("user").get("fullName"));
                var uEmail = cb.lower(root.get("user").get("email"));
                var uPhone = cb.lower(root.get("user").get("phone"));
                var fName = cb.lower(root.get("field").get("fieldName"));

                predicates = cb.and(predicates,
                        cb.or(
                                cb.like(uFullName, like),
                                cb.like(uEmail, like),
                                cb.like(uPhone, like),
                                cb.like(fName, like)
                        )
                );
            }

            return predicates;
        };
    }
}

