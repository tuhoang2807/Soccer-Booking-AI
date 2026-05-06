package com.example.soccer_booking_server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OTP đặt lại mật khẩu");
        message.setText(
                "Mã OTP của bạn là: " + otp + "\n\n" +
                        "Mã có hiệu lực trong 5 phút."
        );
        mailSender.send(message);
    }
}