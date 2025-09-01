package com.hospital_management.repo;


import com.hospital_management.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    List<Review> findByPatientIdOrderBySubmittedDateDesc(Long patientId);
    Optional<Review> findByIdAndPatientId(Long id, Long patientId);

    List<Review> findByDoctorIdAndIsPublicTrue(Long doctorId);
    List<Review> findByAppointmentId(Long appointmentId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctorId = :doctorId AND r.isPublic = true")
    Double getAverageRatingForDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT r FROM Review r WHERE r.doctorId = :doctorId " +
            "AND r.isPublic = true AND r.isVerified = true " +
            "ORDER BY r.submittedDate DESC")
    List<Review> findPublicVerifiedReviewsForDoctor(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.doctorId = :doctorId " +
            "AND r.wouldRecommend = true")
    Long countRecommendationsForDoctor(@Param("doctorId") Long doctorId);
}
