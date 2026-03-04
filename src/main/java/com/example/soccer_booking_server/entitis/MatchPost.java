package com.example.soccer_booking_server.entitis;

import com.example.soccer_booking_server.enums.MatchPostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "match_posts",
        indexes = {
                @Index(name = "idx_match_posts_status", columnList = "status"),
                @Index(name = "idx_match_posts_owner", columnList = "owner_id"),
                @Index(name = "idx_match_posts_booking", columnList = "booking_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_post_id")
    private Long id;

    // booking mà bài này dựa trên (chỉ người đặt booking đó mới được tạo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // chủ bài = người đặt sân (thực tế sẽ bằng booking.user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Users owner;

    // người được chọn làm đối thủ (set khi MATCHED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_user_id")
    private Users matchedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchPostStatus status;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}