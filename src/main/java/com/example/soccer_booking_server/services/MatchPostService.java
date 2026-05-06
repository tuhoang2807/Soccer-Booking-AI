package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.dto.match.MatchApplicantDTO;
import com.example.soccer_booking_server.dto.match.MatchPostDTO;
import com.example.soccer_booking_server.dto.match.MatchPostDetailDTO;
import com.example.soccer_booking_server.dto.match.MatchPostListItemDTO;
import com.example.soccer_booking_server.entitis.*;
import com.example.soccer_booking_server.enums.*;
import com.example.soccer_booking_server.payload.match.*;
import com.example.soccer_booking_server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchPostService {

    private final BookingRepository bookingRepository;
    private final UserRepository usersRepository;
    private final MatchPostRepository matchPostRepository;
    private final MatchApplicationRepository matchApplicationRepository;

    /**
     * currentUserId: lấy từ SecurityContext (Bước controller mình chỉ cách dưới)
     */
    @Transactional
    public MatchPostDTO createPost(Integer currentUserId, CreateMatchPostRequest req) {
        // 1) lock booking để tránh race condition tạo 2 bài
        Booking booking = bookingRepository.findByIdForUpdate(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));

        Users currentUser = usersRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 2) Validate: chỉ người đặt sân mới được đăng
        // (Booking của bạn thường có field getUser() hoặc getUsers() - chỉnh theo entity thực tế)
        Integer bookingOwnerId = booking.getUser().getUserId();
        if (!bookingOwnerId.equals(currentUserId)) {
            throw new RuntimeException("FORBIDDEN_NOT_BOOKING_OWNER");
        }

        // 3) (Tuỳ bạn) validate booking hợp lệ: không bị huỷ, không hết hạn...
        // TODO: nếu Booking có status: check status ở đây.
        if (booking.getStatus() != BookingStatus.DEPOSITED) {
            throw new RuntimeException("ONLY_DEPOSITED_BOOKING_CAN_CREATE_MATCH_POST");
        }
        // 4) Nếu booking đã từng MATCHED -> cấm đăng nữa
        boolean hasMatched = matchPostRepository.existsByBooking_BookingIdAndStatus(req.getBookingId(), MatchPostStatus.MATCHED);
        if (hasMatched) {
            throw new RuntimeException("BOOKING_ALREADY_MATCHED_CANNOT_POST_AGAIN");
        }

        // 5) Nếu đang có OPEN -> cấm tạo bài mới
        boolean hasOpen = matchPostRepository.existsByBooking_BookingIdAndStatus(req.getBookingId(), MatchPostStatus.OPEN);
        if (hasOpen) {
            throw new RuntimeException("ONLY_ONE_OPEN_POST_PER_BOOKING");
        }

        // 6) Tạo post OPEN
        MatchPost post = MatchPost.builder()
                .booking(booking)
                .owner(currentUser)
                .status(MatchPostStatus.OPEN)
                .title(req.getTitle())
                .note(req.getNote())
                .build();

        post = matchPostRepository.save(post);
        return toDTO(post);
    }

    @Transactional
    public void deleteOpenPost(Integer currentUserId, Long postId) {
        MatchPost post = matchPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new RuntimeException("POST_NOT_FOUND"));

        if (!post.getOwner().getUserId().equals(currentUserId)) {
            throw new RuntimeException("FORBIDDEN_NOT_OWNER");
        }
        if (post.getStatus() != MatchPostStatus.OPEN) {
            throw new RuntimeException("ONLY_OPEN_POST_CAN_BE_DELETED");
        }

        post.setStatus(MatchPostStatus.DELETED);
        matchPostRepository.save(post);
    }

    @Transactional
    public void apply(Integer currentUserId, Long postId, ApplyMatchPostRequest req) {
        MatchPost post = matchPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST_NOT_FOUND"));

        if (post.getStatus() != MatchPostStatus.OPEN) {
            throw new RuntimeException("POST_NOT_OPEN");
        }

        // không cho owner tự apply
        if (post.getOwner().getUserId().equals(currentUserId)) {
            throw new RuntimeException("OWNER_CANNOT_APPLY");
        }

        Users applicant = usersRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 1 user chỉ apply 1 lần (DB unique đã chặn, nhưng mình chặn sớm)
        matchApplicationRepository.findByPost_IdAndApplicant_UserId(postId, currentUserId)
                .ifPresent(a -> { throw new RuntimeException("ALREADY_APPLIED"); });

        MatchApplication app = MatchApplication.builder()
                .post(post)
                .applicant(applicant)
                .status(MatchApplicationStatus.PENDING)
                .message(req.getMessage())
                .build();

        matchApplicationRepository.save(app);
    }

    @Transactional
    public MatchPostDTO chooseOpponent(Integer currentUserId, Long postId, ChooseOpponentRequest req) {
        // lock post để tránh 2 lần choose
        MatchPost post = matchPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new RuntimeException("POST_NOT_FOUND"));

        if (!post.getOwner().getUserId().equals(currentUserId)) {
            throw new RuntimeException("FORBIDDEN_NOT_OWNER");
        }
        if (post.getStatus() != MatchPostStatus.OPEN) {
            throw new RuntimeException("POST_NOT_OPEN");
        }

        Integer opponentId = req.getApplicantUserId();

        MatchApplication chosen = matchApplicationRepository
                .findByPost_IdAndApplicant_UserId(postId, opponentId)
                .orElseThrow(() -> new RuntimeException("APPLICATION_NOT_FOUND"));

        if (chosen.getStatus() != MatchApplicationStatus.PENDING) {
            throw new RuntimeException("APPLICATION_NOT_PENDING");
        }

        // Update post -> MATCHED
        Users opponent = chosen.getApplicant();
        post.setMatchedUser(opponent);
        post.setMatchedAt(LocalDateTime.now());
        post.setStatus(MatchPostStatus.MATCHED);
        matchPostRepository.save(post);

        // Update applications: chosen ACCEPTED, others REJECTED
        List<MatchApplication> apps = matchApplicationRepository.findByPost_Id(postId);
        for (MatchApplication a : apps) {
            if (a.getApplicant().getUserId().equals(opponentId)) {
                a.setStatus(MatchApplicationStatus.ACCEPTED);
            } else if (a.getStatus() == MatchApplicationStatus.PENDING) {
                a.setStatus(MatchApplicationStatus.REJECTED);
            }
        }
        matchApplicationRepository.saveAll(apps);

        return toDTO(post);
    }

    private MatchPostDTO toDTO(MatchPost post) {
        return MatchPostDTO.builder()
                .id(post.getId())
                .bookingId(post.getBooking().getBookingId())
                .ownerId(post.getOwner().getUserId())
                .status(post.getStatus())
                .matchedUserId(post.getMatchedUser() == null ? null : post.getMatchedUser().getUserId())
                .title(post.getTitle())
                .note(post.getNote())
                .createdAt(post.getCreatedAt())
                .matchedAt(post.getMatchedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MatchPostListItemDTO> listAll() {
        List<Object[]> rows = matchPostRepository.findAllForList();

        return rows.stream().map(r -> {
            Long postId = (Long) r[0];
            var status = (com.example.soccer_booking_server.enums.MatchPostStatus) r[1];
            String note = (String) r[2];

            Integer ownerId = (Integer) r[3];
            String ownerName = (String) r[4];
            String ownerAvatarUrl = (String) r[5];

            Long bookingId = (Long) r[6];
            Integer fieldId = (Integer) r[7];
            String fieldName = (String) r[8];
            Object fieldType = r[9]; // enum FieldType
            String fieldDescription = (String) r[10];
            java.time.LocalDate bookingDate = (java.time.LocalDate) r[11];
            java.time.LocalTime start = (java.time.LocalTime) r[12];
            java.time.LocalTime end = (java.time.LocalTime) r[13];
            java.math.BigDecimal fieldPrice = (java.math.BigDecimal) r[14];

            Integer matchedUserId = (Integer) r[15];
            String matchedUserName = (String) r[16];

            Long pendingCountLong = (Long) r[17];
            int pendingCount = pendingCountLong == null ? 0 : pendingCountLong.intValue();

            return MatchPostListItemDTO.builder()
                    .id(postId)
                    .status(status)
                    .note(note)

                    .ownerId(ownerId)
                    .ownerName(ownerName)
                    .ownerAvatarUrl(ownerAvatarUrl)

                    .bookingId(bookingId)
                    .fieldId(fieldId)
                    .fieldName(fieldName)
                    .fieldType(fieldType == null ? null : fieldType.toString())
                    .fieldDescription(fieldDescription)
                    .date(bookingDate == null ? null : bookingDate.toString())
                    .start(start == null ? null : start.toString())
                    .end(end == null ? null : end.toString())
                    .price(fieldPrice == null ? null : fieldPrice.toPlainString())

                    .pendingCount(pendingCount)

                    .matchedUserId(matchedUserId)
                    .matchedUserName(matchedUserName)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public MatchPostDetailDTO getDetail(Long postId) {
        MatchPost post = matchPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST_NOT_FOUND"));

        Booking b = post.getBooking();
        Field f = b.getField();
        FieldSlot s = b.getSlot();
        Users o = post.getOwner();
        Users mu = post.getMatchedUser();

        List<MatchApplicantDTO> applicants = matchApplicationRepository
                .findAllByPostIdWithApplicant(postId)
                .stream()
                .map(a -> MatchApplicantDTO.builder()
                        .id(a.getApplicant().getUserId())
                        .name(a.getApplicant().getFullName())
                        .avatarUrl(a.getApplicant().getAvatarUrl())
                        .message(a.getMessage())
                        .status(a.getStatus())
                        .build()
                ).toList();

        return MatchPostDetailDTO.builder()
                .id(post.getId())
                .status(post.getStatus())
                .title(post.getTitle())
                .note(post.getNote())

                .ownerId(o.getUserId())
                .ownerName(o.getFullName())
                .ownerAvatarUrl(o.getAvatarUrl())

                .bookingId(b.getBookingId())
                .fieldId(f.getFieldId())
                .fieldName(f.getFieldName())
                .fieldType(f.getType() == null ? null : f.getType().toString())
                .fieldDescription(f.getDescription())
                .date(b.getBookingDate() == null ? null : b.getBookingDate().toString())
                .start(s.getSlotStart() == null ? null : s.getSlotStart().toString())
                .end(s.getSlotEnd() == null ? null : s.getSlotEnd().toString())
                .price(b.getFieldPrice() == null ? null : b.getFieldPrice().toPlainString())

                .matchedUserId(mu == null ? null : mu.getUserId())
                .matchedUserName(mu == null ? null : mu.getFullName())

                .applicants(applicants)
                .build();
    }
}