package com.hospital_management.repo;


import com.hospital_management.models.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long>, JpaSpecificationExecutor<LabTest> {

    // Find test by order number
    Optional<LabTest> findByOrderNumber(String orderNumber);

    // Find pending tests for patient
    @Query("SELECT lt FROM LabTest lt WHERE lt.patientId = :patientId " +
            "AND lt.status IN ('ORDERED', 'SCHEDULED', 'SAMPLE_COLLECTED', 'IN_PROGRESS') " +
            "ORDER BY lt.orderDate ASC")
    List<LabTest> findPendingTests(@Param("patientId") Long patientId);

    // Find tests by patient ID
    List<LabTest> findByPatientIdOrderByOrderDateDesc(Long patientId);

    // Find tests by doctor ID
    List<LabTest> findByDoctorIdOrderByOrderDateDesc(Long doctorId);

    // Find tests by status
    List<LabTest> findByStatusOrderByOrderDateDesc(LabTest.TestStatus status);

    // Find urgent tests
    @Query("SELECT lt FROM LabTest lt WHERE lt.urgency IN ('URGENT', 'STAT', 'EMERGENCY') " +
            "AND lt.status NOT IN ('COMPLETED', 'CANCELLED') " +
            "ORDER BY lt.urgency DESC, lt.orderDate ASC")
    List<LabTest> findUrgentTests();

    // Find tests scheduled for today
    @Query("SELECT lt FROM LabTest lt WHERE DATE(lt.scheduledDate) = CURRENT_DATE " +
            "AND lt.status IN ('SCHEDULED', 'ORDERED') " +
            "ORDER BY lt.scheduledDate ASC")
    List<LabTest> findTestsScheduledToday();

    // Find overdue tests
    @Query("SELECT lt FROM LabTest lt WHERE lt.estimatedCompletionDate < :currentDateTime " +
            "AND lt.status NOT IN ('COMPLETED', 'CANCELLED') " +
            "ORDER BY lt.estimatedCompletionDate ASC")
    List<LabTest> findOverdueTests(@Param("currentDateTime") LocalDateTime currentDateTime);

    // Find tests by test type
    List<LabTest> findByTestTypeAndPatientIdOrderByOrderDateDesc(String testType, Long patientId);

    // Find home collection tests
    @Query("SELECT lt FROM LabTest lt WHERE lt.isHomeCollection = true " +
            "AND lt.status = 'ORDERED' " +
            "ORDER BY lt.scheduledDate ASC")
    List<LabTest> findHomeCollectionTests();

    // Find tests requiring fasting
    @Query("SELECT lt FROM LabTest lt WHERE lt.isFastingRequired = true " +
            "AND lt.patientId = :patientId " +
            "AND lt.status = 'ORDERED' " +
            "ORDER BY lt.scheduledDate ASC")
    List<LabTest> findFastingRequiredTests(@Param("patientId") Long patientId);

    // Count tests by status
    @Query("SELECT lt.status, COUNT(lt) FROM LabTest lt GROUP BY lt.status")
    List<Object[]> countTestsByStatus();

    // Find tests in date range
    @Query("SELECT lt FROM LabTest lt WHERE lt.orderDate BETWEEN :startDate AND :endDate " +
            "ORDER BY lt.orderDate DESC")
    List<LabTest> findTestsInDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Find completed tests without results
    @Query("SELECT lt FROM LabTest lt WHERE lt.status = 'COMPLETED' " +
            "AND lt.labResults IS EMPTY " +
            "ORDER BY lt.orderDate ASC")
    List<LabTest> findCompletedTestsWithoutResults();

    // Search tests by test name
    @Query("SELECT lt FROM LabTest lt WHERE lt.patientId = :patientId " +
            "AND LOWER(lt.testName) LIKE LOWER(CONCAT('%', :testName, '%')) " +
            "ORDER BY lt.orderDate DESC")
    List<LabTest> searchTestsByName(@Param("patientId") Long patientId,
                                    @Param("testName") String testName);
}
