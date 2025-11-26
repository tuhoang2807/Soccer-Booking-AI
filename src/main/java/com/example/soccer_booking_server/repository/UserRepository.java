package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByPhone(String phone);
}
