package com.hospital_management.repo;



import com.hospital_management.models.DischargeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, Long>, JpaSpecificationExecutor<DischargeSummary> {

    List<DischargeSummary> findByPatientIdOrderByDischargeDateDesc(Long patientId);
    Optional<DischargeSummary> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT d FROM DischargeSummary d WHERE d.patientId = :patientId " +
            "AND d.dischargeDate BETWEEN :startDate AND :endDate " +
            "ORDER BY d.dischargeDate DESC")
    List<DischargeSummary> findByPatientAndDateRange(@Param("patientId") Long patientId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DischargeSummary d WHERE d.followUpDate = :date")
    List<DischargeSummary> findWithFollowUpDate(@Param("date") LocalDate date);
}
