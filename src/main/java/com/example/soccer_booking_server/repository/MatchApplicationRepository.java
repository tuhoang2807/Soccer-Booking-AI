package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.MatchApplication;
import com.example.soccer_booking_server.enums.MatchApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchApplicationRepository extends JpaRepository<MatchApplication, Long> {

    Optional<MatchApplication> findByPost_IdAndApplicant_UserId(Long postId, Integer applicantId);

    List<MatchApplication> findByPost_Id(Long postId);

    List<MatchApplication> findByPost_IdAndStatus(Long postId, MatchApplicationStatus status);

    @Query("""
  select ma
  from MatchApplication ma
    join fetch ma.applicant u
  where ma.post.id = :postId
  order by ma.createdAt desc
""")
    List<MatchApplication> findAllByPostIdWithApplicant(@Param("postId") Long postId);
}