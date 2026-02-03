package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.entitis.Users;
import com.example.soccer_booking_server.exception.NotFoundException;
import com.example.soccer_booking_server.exception.ValidException;
import com.example.soccer_booking_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

    private void requireNotEmpty(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidException(message);
        }
    }

    public Users createUser(Users user) {
        requireNotEmpty(user.getFullName(), "Tên không được để trống!");
        requireNotEmpty(user.getEmail(), "Email không được để trống!");
        requireNotEmpty(user.getPhone(), "Số điện thoại không được để trống!");
        requireNotEmpty(user.getPasswordHash(), "Password không được để trống!");
        requireNotEmpty(user.getTeamLeaderName(),"Tên đội trưởng không được để trống!");
        requireNotEmpty(user.getTeamName(),"Tên đội không được để trống!");

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        String phoneRegex = "^[0-9]{10}$";

        if (!user.getEmail().matches(emailRegex)) {
            throw new ValidException("Email không đúng định dạng!");
        }

        if (!user.getPhone().matches(phoneRegex)) {
            throw new ValidException("Số điện thoại không đúng định dạng!");
        }

        if (user.getPasswordHash().length() < 6) {
            throw new ValidException("Password phải có ít nhất 6 ký tự!");
        }

        Optional<Users> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new ValidException("Email đã được sử dụng!");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        return userRepository.save(user);
    }


    public Users updateUser(Integer id, Users userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getFullName() != null) {
                requireNotEmpty(userDetails.getFullName(), "Tên không được để trống!");
                user.setFullName(userDetails.getFullName());
            }

            if (userDetails.getEmail() != null) {
                requireNotEmpty(userDetails.getEmail(), "Email không được để trống!");
                String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
                if (!userDetails.getEmail().matches(emailRegex)) {
                    throw new ValidException("Email không đúng định dạng!");
                }

                Optional<Users> existingUser = userRepository.findByEmail(userDetails.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(id)) {
                    throw new ValidException("Email đã được sử dụng!");
                }
                user.setEmail(userDetails.getEmail());
            }

            if (userDetails.getPhone() != null) {
                requireNotEmpty(userDetails.getPhone(), "Số điện thoại không được để trống!");
                String phoneRegex = "^[0-9]{10}$";
                if (!userDetails.getPhone().matches(phoneRegex)) {
                    throw new ValidException("Số điện thoại không đúng định dạng!");
                }
                Optional<Users> existingUser = userRepository.findByPhone(userDetails.getPhone());
                if (existingUser.isPresent() && !existingUser.get().getUserId().equals(id)) {
                    throw new ValidException("Số điện thoại đã được sử dụng");
                }
                user.setPhone(userDetails.getPhone());
            }

            if (userDetails.getPasswordHash() != null) {
                requireNotEmpty(userDetails.getPasswordHash(), "Password không được để trống!");
                if (userDetails.getPasswordHash().length() < 6) {
                    throw new ValidException("Password phải có ít nhất 6 ký tự!");
                }
                user.setPasswordHash(userDetails.getPasswordHash());
            }

            if (userDetails.getAvatarUrl() != null) {
                user.setAvatarUrl(userDetails.getAvatarUrl());
            }
            if (userDetails.getAddress() != null) {
                user.setAddress(userDetails.getAddress());
            }
            if (userDetails.getRole() != null) {
                user.setRole(userDetails.getRole());
            }
            if (userDetails.getMatchStatus() != null) {
                user.setMatchStatus(userDetails.getMatchStatus());
            }
            if (userDetails.getLoyaltyPoints() != null) {
                user.setLoyaltyPoints(userDetails.getLoyaltyPoints());
            }
            if (userDetails.getIsActive() != null) {
                user.setIsActive(userDetails.getIsActive());
            }

            return userRepository.save(user);
        }).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public BigDecimal getUserCoin(Integer userId) {
       return userRepository.findCoinBalanceByUserId(userId);
    }
}
