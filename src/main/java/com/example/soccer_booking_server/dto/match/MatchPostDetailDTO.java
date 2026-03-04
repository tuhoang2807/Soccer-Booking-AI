package com.example.soccer_booking_server.dto.match;

import com.example.soccer_booking_server.enums.MatchPostStatus;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPostDetailDTO {
    private Long id;
    private MatchPostStatus status;

    private String title;
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
    private String fieldDescription;
    private String date;
    private String start;
    private String end;
    private String price;

    // matched
    private Integer matchedUserId;
    private String matchedUserName;

    private List<MatchApplicantDTO> applicants;
}