package com.hospital_management.repo;

import com.hospital_management.models.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long>, JpaSpecificationExecutor<MedicalRecord> {

    // Find record by ID and patient ID for security
    Optional<MedicalRecord> findByIdAndPatientId(Long id, Long patientId);

    // Find records by patient ID
    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(Long patientId);

    // Find records by record number
    Optional<MedicalRecord> findByRecordNumber(String recordNumber);

    // Find records by doctor ID
    List<MedicalRecord> findByDoctorIdOrderByRecordDateDesc(Long doctorId);

    // Find records by appointment ID
    List<MedicalRecord> findByAppointmentId(Long appointmentId);

    // Find records by record type
    List<MedicalRecord> findByRecordTypeAndPatientIdOrderByRecordDateDesc(String recordType, Long patientId);

    // Find records by status
    List<MedicalRecord> findByRecordStatusOrderByRecordDateDesc(MedicalRecord.RecordStatus recordStatus);

    // Find records in date range
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND mr.recordDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findPatientRecordsInDateRange(@Param("patientId") Long patientId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // Find emergency records
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.isEmergency = true " +
            "AND mr.recordDate >= :since " +
            "ORDER BY mr.visitDateTime DESC")
    List<MedicalRecord> findEmergencyRecords(@Param("since") LocalDate since);

    // Find follow-up records
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.isFollowUp = true " +
            "AND mr.patientId = :patientId " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findFollowUpRecords(@Param("patientId") Long patientId);

    // Find records requiring follow-up
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.followUpDate <= :date " +
            "AND mr.recordStatus = 'SIGNED' " +
            "AND NOT EXISTS (SELECT f FROM MedicalRecord f WHERE f.previousRecordId = mr.id) " +
            "ORDER BY mr.followUpDate ASC")
    List<MedicalRecord> findRecordsRequiringFollowUp(@Param("date") LocalDate date);

    // Find unsigned records
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.recordStatus IN ('DRAFT', 'ACTIVE') " +
            "AND mr.doctorId = :doctorId " +
            "ORDER BY mr.visitDateTime ASC")
    List<MedicalRecord> findUnsignedRecordsByDoctor(@Param("doctorId") Long doctorId);

    // Find recent records for patient
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findRecentRecordsForPatient(@Param("patientId") Long patientId);

    // Search medical records by partial diagnosis text (FIXED - removed LOWER() on CLOB)
    @Query("SELECT mr FROM MedicalRecord mr " +
            "WHERE mr.patientId = :patientId " +
            "AND mr.diagnosis LIKE CONCAT('%', :diagnosis, '%') " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> searchRecordsByDiagnosis(@Param("patientId") Long patientId,
                                                 @Param("diagnosis") String diagnosis);

    // Find records with specific ICD codes
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND mr.icdCodes LIKE CONCAT('%', :icdCode, '%') " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findRecordsByIcdCode(@Param("patientId") Long patientId,
                                             @Param("icdCode") String icdCode);

    // Find records by confidentiality level
    List<MedicalRecord> findByConfidentialityLevelAndPatientIdOrderByRecordDateDesc(
            MedicalRecord.ConfidentialityLevel confidentialityLevel, Long patientId);

    // Count records by status
    @Query("SELECT mr.recordStatus, COUNT(mr) FROM MedicalRecord mr GROUP BY mr.recordStatus")
    List<Object[]> countRecordsByStatus();

    // Find records with vital signs
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND (mr.temperature IS NOT NULL OR mr.bloodPressureSystolic IS NOT NULL " +
            "OR mr.heartRate IS NOT NULL OR mr.weight IS NOT NULL) " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findRecordsWithVitalSigns(@Param("patientId") Long patientId);

    // Find records with attachments
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr " +
            "JOIN mr.attachments a " +
            "WHERE mr.patientId = :patientId " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findRecordsWithAttachments(@Param("patientId") Long patientId);

    // Find records requiring discharge
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.recordType = 'INPATIENT' " +
            "AND mr.dischargeDate IS NULL " +
            "AND mr.recordStatus = 'SIGNED' " +
            "ORDER BY mr.visitDateTime ASC")
    List<MedicalRecord> findRecordsRequiringDischarge();

    // FIXED: Find chronic condition records (removed LOWER() on CLOB fields)
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND (mr.diagnosis LIKE '%diabetes%' " +
            "OR mr.diagnosis LIKE '%hypertension%' " +
            "OR mr.diagnosis LIKE '%asthma%' " +
            "OR mr.diagnosis LIKE '%chronic%' " +
            "OR mr.diagnosis LIKE '%Diabetes%' " +
            "OR mr.diagnosis LIKE '%Hypertension%' " +
            "OR mr.diagnosis LIKE '%Asthma%' " +
            "OR mr.diagnosis LIKE '%Chronic%') " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findChronicConditionRecords(@Param("patientId") Long patientId);

    // Find medication history
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND mr.currentMedications IS NOT NULL " +
            "AND mr.currentMedications != '' " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findMedicationHistory(@Param("patientId") Long patientId);

    // Additional useful methods
    @Query("SELECT COUNT(mr) FROM MedicalRecord mr WHERE mr.patientId = :patientId")
    Long countByPatientId(@Param("patientId") Long patientId);

    // Find records by treatment plan content
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND mr.treatmentPlan LIKE CONCAT('%', :treatment, '%') " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> searchRecordsByTreatment(@Param("patientId") Long patientId,
                                                 @Param("treatment") String treatment);

    // Find records with specific procedures
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
            "AND mr.proceduresPerformed LIKE CONCAT('%', :procedure, '%') " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findRecordsByProcedure(@Param("patientId") Long patientId,
                                               @Param("procedure") String procedure);

    // Find records by doctor and date range
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.doctorId = :doctorId " +
            "AND mr.recordDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mr.recordDate DESC")
    List<MedicalRecord> findDoctorRecordsInDateRange(@Param("doctorId") Long doctorId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}
