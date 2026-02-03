package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.LoginResponseDto;
import com.example.soccer_booking_server.dto.ResponseFormat;
import com.example.soccer_booking_server.dto.UserInfoDTO;
import com.example.soccer_booking_server.entitis.RefreshToken;
import com.example.soccer_booking_server.entitis.Users;
import com.example.soccer_booking_server.enums.MatchStatus;
import com.example.soccer_booking_server.enums.Role;

import com.example.soccer_booking_server.payload.LoginRequest;
import com.example.soccer_booking_server.payload.RegisterRequest;
import com.example.soccer_booking_server.repository.RefreshTokenRepository;
import com.example.soccer_booking_server.repository.UserRepository;
import com.example.soccer_booking_server.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository usersRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ResponseFormat<?>> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            Users user = usersRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này!"));

            String accessToken = jwtUtils.generateAccessToken(user);

            refreshTokenRepository.deleteByUser(user);
            String refreshTokenStr = jwtUtils.generateRefreshToken();

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenStr)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();
            refreshTokenRepository.save(refreshToken);

            // ✅ user info tối thiểu
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(user.getUserId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .teamName(user.getTeamName())
                    .teamLeaderName(user.getTeamLeaderName())
                    .coinBalance(user.getCoinBalance())
                    .build();

            // ✅ response gộp token + user
            LoginResponseDto data = LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenStr)
                    .user(userInfo)
                    .build();

            return ResponseEntity.ok(new ResponseFormat<>(200, "Đăng nhập thành công", data));
        } catch (Exception ex) {
            // 🚀 nên trả 401 thay vì NotFound
            return ResponseEntity.status(401)
                    .body(new ResponseFormat<>(401, "Sai email hoặc mật khẩu!", null));
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        String accessToken = jwtUtils.generateAccessToken(token.getUser());
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email đã được sử dụng"));
        }
        if (usersRepository.findByPhone(request.getPhone()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Số điện thoại này đã được sử dụng"));
        }

        Users newUser = Users.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .matchStatus(MatchStatus.NOT_LOOKING)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .loyaltyPoints(0)
                .teamName(request.getTeamName())
                .teamLeaderName(request.getTeamLeadName())
                .role(Role.USER)
                .build();

        usersRepository.save(newUser);
        return ResponseEntity.ok(Map.of(
                "message", "Đăng ký người dùng thành công",
                "userId", newUser.getUserId()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(refreshTokenRepository::delete);
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }
}

