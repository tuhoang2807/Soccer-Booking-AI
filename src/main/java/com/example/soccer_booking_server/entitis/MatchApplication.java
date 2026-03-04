package com.example.soccer_booking_server.entitis;

import com.example.soccer_booking_server.enums.MatchApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "match_applications",
        uniqueConstraints = {
                // 1 user chỉ apply 1 lần cho 1 post
                @UniqueConstraint(name = "uk_post_applicant", columnNames = {"match_post_id", "applicant_id"})
        },
        indexes = {
                @Index(name = "idx_match_app_status", columnList = "status"),
                @Index(name = "idx_match_app_post", columnList = "match_post_id"),
                @Index(name = "idx_match_app_applicant", columnList = "applicant_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_post_id", nullable = false)
    private MatchPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Users applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchApplicationStatus status;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}