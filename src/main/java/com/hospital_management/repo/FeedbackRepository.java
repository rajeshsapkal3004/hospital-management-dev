package com.hospital_management.repo;

import com.hospital_management.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {

    List<Feedback> findByPatientIdOrderBySubmittedDateDesc(Long patientId);
    Optional<Feedback> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT f FROM Feedback f WHERE f.status = :status ORDER BY f.submittedDate ASC")
    List<Feedback> findByStatus(@Param("status") String status);

    @Query("SELECT f FROM Feedback f WHERE f.category = :category " +
            "AND f.submittedDate >= :since ORDER BY f.submittedDate DESC")
    List<Feedback> findByCategoryAndDateRange(@Param("category") String category,
                                              @Param("since") LocalDateTime since);

    @Query("SELECT f.category, AVG(f.rating) FROM Feedback f " +
            "WHERE f.rating IS NOT NULL GROUP BY f.category")
    List<Object[]> getAverageRatingByCategory();
}
