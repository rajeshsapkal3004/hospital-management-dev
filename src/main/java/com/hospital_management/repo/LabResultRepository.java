package com.hospital_management.repo;


import com.hospital_management.models.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Long>, JpaSpecificationExecutor<LabResult> {

    // Find result by ID and patient ID for security
    Optional<LabResult> findByIdAndPatientId(Long id, Long patientId);

    // Find results by patient ID
    List<LabResult> findByPatientIdOrderByTestDateDesc(Long patientId);

    // Find results by test type
    List<LabResult> findByTestTypeAndPatientIdOrderByTestDateDesc(String testType, Long patientId);

    // Find results by reference number
    Optional<LabResult> findByReferenceNumber(String referenceNumber);

    // Find critical results
    @Query("SELECT lr FROM LabResult lr WHERE lr.isCritical = true " +
            "AND lr.testDate >= :since " +
            "ORDER BY lr.testDate DESC")
    List<LabResult> findCriticalResults(@Param("since") LocalDateTime since);

    // Find abnormal results
    @Query("SELECT lr FROM LabResult lr WHERE lr.isAbnormal = true " +
            "AND lr.patientId = :patientId " +
            "ORDER BY lr.testDate DESC")
    List<LabResult> findAbnormalResultsForPatient(@Param("patientId") Long patientId);

    // Find results by status
    List<LabResult> findByStatusOrderByTestDateDesc(LabResult.ResultStatus status);

    // Find pending verification
    @Query("SELECT lr FROM LabResult lr WHERE lr.status = 'COMPLETED' " +
            "AND lr.verifiedDate IS NULL " +
            "ORDER BY lr.testDate ASC")
    List<LabResult> findPendingVerification();

    // Find results in date range
    @Query("SELECT lr FROM LabResult lr WHERE lr.patientId = :patientId " +
            "AND lr.testDate BETWEEN :startDate AND :endDate " +
            "ORDER BY lr.testDate DESC")
    List<LabResult> findPatientResultsInDateRange(@Param("patientId") Long patientId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Find results by technician
    List<LabResult> findByTechnicianIdOrderByTestDateDesc(Long technicianId);

    // Find results requiring attention
    @Query("SELECT lr FROM LabResult lr WHERE (lr.isCritical = true OR lr.isAbnormal = true " +
            "OR lr.resultFlag IN ('CRITICAL_HIGH', 'CRITICAL_LOW')) " +
            "AND lr.testDate >= :since " +
            "ORDER BY lr.testDate DESC")
    List<LabResult> findResultsRequiringAttention(@Param("since") LocalDateTime since);

    // Count results by status
    @Query("SELECT lr.status, COUNT(lr) FROM LabResult lr GROUP BY lr.status")
    List<Object[]> countResultsByStatus();

    // Find recent results for patient
    @Query("SELECT lr FROM LabResult lr WHERE lr.patientId = :patientId " +
            "ORDER BY lr.testDate DESC")
    List<LabResult> findRecentResultsForPatient(@Param("patientId") Long patientId);

    // Find results with specific flag
    List<LabResult> findByResultFlagAndPatientIdOrderByTestDateDesc(LabResult.ResultFlag flag, Long patientId);

    // Find results by lab test ID
    List<LabResult> findByLabTestIdOrderByTestDateDesc(Long labTestId);

    // Search results by test name
    @Query("SELECT lr FROM LabResult lr WHERE lr.patientId = :patientId " +
            "AND LOWER(lr.testName) LIKE LOWER(CONCAT('%', :testName, '%')) " +
            "ORDER BY lr.testDate DESC")
    List<LabResult> searchResultsByTestName(@Param("patientId") Long patientId,
                                            @Param("testName") String testName);

    // Add these methods to LabResultRepository interface

    @Query("SELECT COUNT(lr) FROM LabResult lr WHERE lr.patient.id = :patientId " +
            "AND lr.status IN ('PENDING', 'IN_PROGRESS')")
    Integer countPendingForPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(lr) FROM LabResult lr WHERE lr.patient.id = :patientId " +
            "AND lr.status = 'COMPLETED'")
    Integer countCompletedForPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(lr) FROM LabResult lr WHERE lr.patient.id = :patientId " +
            "AND lr.isCritical = true")
    Integer countCriticalResultsForPatient(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(lr) FROM LabResult lr WHERE lr.patient.id = :patientId " +
            "AND lr.testDate >= :since")
    Integer countRecentResultsForPatient(@Param("patientId") Long patientId,
                                         @Param("since") LocalDateTime since);

}
