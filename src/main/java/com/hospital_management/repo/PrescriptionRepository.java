package com.hospital_management.repo;



import com.hospital_management.models.Prescription;
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
public interface PrescriptionRepository extends JpaRepository<Prescription, Long>, JpaSpecificationExecutor<Prescription> {

    // Find prescription by ID and patient ID for security
    Optional<Prescription> findByIdAndPatientId(Long id, Long patientId);

    // Find prescriptions by patient ID
    List<Prescription> findByPatientIdOrderByPrescribedDateDesc(Long patientId);

    // Find active prescriptions for patient
    @Query("SELECT p FROM Prescription p WHERE p.patientId = :patientId " +
            "AND p.status = 'ACTIVE' " +
            "AND (p.validUntil IS NULL OR p.validUntil >= CURRENT_DATE) " +
            "ORDER BY p.prescribedDate DESC")
    List<Prescription> findActivePrescriptions(@Param("patientId") Long patientId);

    // Find prescription by prescription number
    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    // Find prescriptions by doctor ID
    List<Prescription> findByDoctorIdOrderByPrescribedDateDesc(Long doctorId);

    // Find prescriptions by status
    List<Prescription> findByStatusOrderByPrescribedDateDesc(Prescription.PrescriptionStatus status);

    // Find expiring prescriptions
    @Query("SELECT p FROM Prescription p WHERE p.validUntil BETWEEN CURRENT_DATE AND :endDate " +
            "AND p.status = 'ACTIVE' " +
            "ORDER BY p.validUntil ASC")
    List<Prescription> findExpiringPrescriptions(@Param("endDate") LocalDate endDate);

    // Find expired prescriptions
    @Query("SELECT p FROM Prescription p WHERE p.validUntil < CURRENT_DATE " +
            "AND p.status = 'ACTIVE' " +
            "ORDER BY p.validUntil DESC")
    List<Prescription> findExpiredPrescriptions();

    // Find prescriptions in date range
    @Query("SELECT p FROM Prescription p WHERE p.patientId = :patientId " +
            "AND p.prescribedDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.prescribedDate DESC")
    List<Prescription> findPatientPrescriptionsInDateRange(@Param("patientId") Long patientId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    // Find undispensed prescriptions
    @Query("SELECT p FROM Prescription p WHERE p.status IN ('ACTIVE', 'PARTIALLY_DISPENSED') " +
            "AND (p.validUntil IS NULL OR p.validUntil >= CURRENT_DATE) " +
            "ORDER BY p.prescribedDate ASC")
    List<Prescription> findUndispensedPrescriptions();

    // Find prescriptions by medical record ID
    List<Prescription> findByMedicalRecordIdOrderByPrescribedDateDesc(Long medicalRecordId);

    // Find prescriptions by appointment ID
    List<Prescription> findByAppointmentIdOrderByPrescribedDateDesc(Long appointmentId);

    // Find repeatable prescriptions
    @Query("SELECT p FROM Prescription p WHERE p.isRepeatable = true " +
            "AND p.patientId = :patientId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.repeatsUsed < p.repeatCount " +
            "ORDER BY p.prescribedDate DESC")
    List<Prescription> findRepeatablePrescriptions(@Param("patientId") Long patientId);

    // Find prescriptions by priority
    List<Prescription> findByPriorityAndStatusOrderByPrescribedDateDesc(
            Prescription.PrescriptionPriority priority,
            Prescription.PrescriptionStatus status
    );

    // Search prescriptions by medication name
    @Query("SELECT DISTINCT p FROM Prescription p " +
            "JOIN p.prescriptionItems pi " +
            "WHERE p.patientId = :patientId " +
            "AND LOWER(pi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%')) " +
            "ORDER BY p.prescribedDate DESC")
    List<Prescription> searchPrescriptionsByMedication(@Param("patientId") Long patientId,
                                                       @Param("medicationName") String medicationName);

    // Count prescriptions by status
    @Query("SELECT p.status, COUNT(p) FROM Prescription p GROUP BY p.status")
    List<Object[]> countPrescriptionsByStatus();

    // Find recent prescriptions for patient
    @Query("SELECT p FROM Prescription p WHERE p.patientId = :patientId " +
            "ORDER BY p.prescribedDate DESC")
    List<Prescription> findRecentPrescriptionsForPatient(@Param("patientId") Long patientId);

    // Find prescriptions dispensed by pharmacy
    @Query("SELECT p FROM Prescription p WHERE p.pharmacyName = :pharmacyName " +
            "AND p.dispensedDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.dispensedDate DESC")
    List<Prescription> findPrescriptionsDispensedByPharmacy(@Param("pharmacyName") String pharmacyName,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    // Find electronic prescriptions
    @Query("SELECT p FROM Prescription p WHERE p.isElectronic = true " +
            "AND p.prescribedDate >= :since " +
            "ORDER BY p.prescribedDate DESC")
    List<Prescription> findElectronicPrescriptions(@Param("since") LocalDateTime since);

    // Find prescriptions requiring dispensing
    @Query("SELECT p FROM Prescription p WHERE p.status = 'ACTIVE' " +
            "AND (p.validUntil IS NULL OR p.validUntil >= CURRENT_DATE) " +
            "AND p.dispensedDate IS NULL " +
            "ORDER BY p.priority DESC, p.prescribedDate ASC")
    List<Prescription> findPrescriptionsRequiringDispensing();
}
