package com.example.soccer_booking_server.dto.match;

import com.example.soccer_booking_server.enums.MatchPostStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPostListItemDTO {
    private Long id;
    private MatchPostStatus status;
    private String note;

    // owner
    private Integer ownerId;
    private String ownerName;
    private String ownerAvatarUrl;

    // booking snapshot
    private Long bookingId;
    private Integer fieldId;
    private String fieldName;
    private String fieldType;
    private String fieldDescription; // ✅ dùng Field.description thay vì address
    private String date;             // yyyy-MM-dd
    private String start;            // HH:mm:ss
    private String end;              // HH:mm:ss
    private String price;            // dùng String để không mất BigDecimal

    private Integer pendingCount;

    // matched
    private Integer matchedUserId;
    private String matchedUserName;
}