package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.match.MatchPostDTO;
import com.example.soccer_booking_server.entitis.Users;
import com.example.soccer_booking_server.payload.match.*;
import com.example.soccer_booking_server.repository.UserRepository;
import com.example.soccer_booking_server.services.MatchPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/match-posts")
@RequiredArgsConstructor
@Tag(name = "Matchmaking", description = "API ghép đối: tạo bài, apply, chọn đối thủ")
public class MatchPostController {

    private final MatchPostService matchPostService;
    private final UserRepository userRepository;

    /**
     * Lấy userId từ email trong Authentication
     */
    private Integer currentUserId(Authentication auth) {

        String email = auth.getName(); // ví dụ: Admin@gmail.com

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND_BY_EMAIL: " + email));

        return user.getUserId();
    }

    @Operation(summary = "Tạo bài ghép đối cho 1 booking (chỉ người đặt sân mới được tạo)")
    @PostMapping
    public ResponseEntity<MatchPostDTO> create(
            Authentication auth,
            @RequestBody CreateMatchPostRequest req
    ) {
        return ResponseEntity.ok(
                matchPostService.createPost(currentUserId(auth), req)
        );
    }

    @Operation(summary = "Xóa bài ghép đối (chỉ xóa được khi status=OPEN)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> delete(
            Authentication auth,
            @PathVariable Long postId
    ) {
        matchPostService.deleteOpenPost(currentUserId(auth), postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Apply vào bài ghép đối (chỉ apply được khi status=OPEN và không phải owner)")
    @PostMapping("/{postId}/apply")
    public ResponseEntity<?> apply(
            Authentication auth,
            @PathVariable Long postId,
            @RequestBody ApplyMatchPostRequest req
    ) {
        matchPostService.apply(currentUserId(auth), postId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Chủ bài chọn đối thủ (post chuyển MATCHED, app chosen=ACCEPTED, còn lại REJECTED)")
    @PostMapping("/{postId}/choose-opponent")
    public ResponseEntity<MatchPostDTO> choose(
            Authentication auth,
            @PathVariable Long postId,
            @RequestBody ChooseOpponentRequest req
    ) {
        return ResponseEntity.ok(
                matchPostService.chooseOpponent(currentUserId(auth), postId, req)
        );
    }

    @Operation(summary = "Lấy danh sách tất cả bài ghép đối")
    @GetMapping
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(matchPostService.listAll());
    }

    @Operation(summary = "Lấy chi tiết 1 bài ghép đối (kèm applicants)")
    @GetMapping("/{postId}")
    public ResponseEntity<?> detail(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(matchPostService.getDetail(postId));
    }
}