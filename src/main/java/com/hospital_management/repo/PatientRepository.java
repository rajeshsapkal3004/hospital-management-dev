package com.hospital_management.repo;

import com.hospital_management.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Basic queries
    Optional<Patient> findByPatientId(String patientId);

    Optional<Patient> findByInsuranceNumber(String insuranceNumber);

    @Query("SELECT p FROM Patient p WHERE p.username = :username")
    Optional<Patient> findByUsername(@Param("username") String username);

    @Query("SELECT p FROM Patient p WHERE p.email = :email")
    Optional<Patient> findByEmail(@Param("email") String email);

    // Existence checks
    boolean existsByPatientId(String patientId);

    boolean existsByInsuranceNumber(String insuranceNumber);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Patient p WHERE p.patientId = :patientId AND p.id != :id")
    boolean existsByPatientIdAndIdNot(@Param("patientId") String patientId, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Patient p WHERE p.insuranceNumber = :insuranceNumber AND p.id != :id")
    boolean existsByInsuranceNumberAndIdNot(@Param("insuranceNumber") String insuranceNumber, @Param("id") Long id);

    // Active status queries
    List<Patient> findByIsActivePatientTrue();

    List<Patient> findByIsActivePatientFalse();

    @Query("SELECT p FROM Patient p WHERE p.isActivePatient = true AND p.enabled = true")
    List<Patient> findAllActivePatients();

    // Age-based queries
    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth >= :date")
    List<Patient> findPatientsBornAfter(@Param("date") LocalDate date);

    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth <= :date")
    List<Patient> findPatientsBornBefore(@Param("date") LocalDate date);

    @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Patient> findPatientsBornBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) >= :minAge")
    List<Patient> findPatientsWithMinimumAge(@Param("minAge") Integer minAge);

    @Query("SELECT p FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
    List<Patient> findPatientsByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Query("SELECT p FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) < :age")
    List<Patient> findPatientsUnderAge(@Param("age") Integer age);

    // Blood type queries
    List<Patient> findByBloodType(String bloodType);

    @Query("SELECT DISTINCT p.bloodType FROM Patient p WHERE p.bloodType IS NOT NULL ORDER BY p.bloodType")
    List<String> findAllBloodTypes();

    @Query("SELECT p FROM Patient p WHERE p.bloodType IN :bloodTypes")
    List<Patient> findByBloodTypeIn(@Param("bloodTypes") List<String> bloodTypes);

    // Insurance queries
    List<Patient> findByInsuranceProvider(String insuranceProvider);

    @Query("SELECT DISTINCT p.insuranceProvider FROM Patient p WHERE p.insuranceProvider IS NOT NULL ORDER BY p.insuranceProvider")
    List<String> findAllInsuranceProviders();

    @Query("SELECT p FROM Patient p WHERE p.insuranceProvider IS NULL OR p.insuranceNumber IS NULL")
    List<Patient> findPatientsWithoutInsurance();

    @Query("SELECT p FROM Patient p WHERE p.insuranceProvider IS NOT NULL AND p.insuranceNumber IS NOT NULL")
    List<Patient> findPatientsWithInsurance();

    // Medical condition queries
    @Query("SELECT p FROM Patient p JOIN p.allergies a WHERE a = :allergy")
    List<Patient> findByAllergy(@Param("allergy") String allergy);

    @Query("SELECT p FROM Patient p JOIN p.medicalConditions mc WHERE mc = :condition")
    List<Patient> findByMedicalCondition(@Param("condition") String condition);

    @Query("SELECT p FROM Patient p JOIN p.currentMedications cm WHERE cm = :medication")
    List<Patient> findByCurrentMedication(@Param("medication") String medication);

    @Query("SELECT p FROM Patient p WHERE SIZE(p.allergies) > 0")
    List<Patient> findPatientsWithAllergies();

    @Query("SELECT p FROM Patient p WHERE SIZE(p.medicalConditions) > 0")
    List<Patient> findPatientsWithMedicalConditions();

    @Query("SELECT p FROM Patient p WHERE SIZE(p.currentMedications) > 0")
    List<Patient> findPatientsWithCurrentMedications();

    // Emergency contact queries
    @Query("SELECT p FROM Patient p WHERE p.emergencyContactName = :contactName")
    List<Patient> findByEmergencyContactName(@Param("contactName") String contactName);

    @Query("SELECT p FROM Patient p WHERE p.emergencyContactPhone = :contactPhone")
    List<Patient> findByEmergencyContactPhone(@Param("contactPhone") String contactPhone);

    // Physical attributes queries
    @Query("SELECT p FROM Patient p WHERE p.heightCm BETWEEN :minHeight AND :maxHeight")
    List<Patient> findByHeightRange(@Param("minHeight") Double minHeight, @Param("maxHeight") Double maxHeight);

    @Query("SELECT p FROM Patient p WHERE p.weightKg BETWEEN :minWeight AND :maxWeight")
    List<Patient> findByWeightRange(@Param("minWeight") Double minWeight, @Param("maxWeight") Double maxWeight);

    @Query("SELECT p FROM Patient p WHERE p.heightCm IS NOT NULL AND p.weightKg IS NOT NULL")
    List<Patient> findPatientsWithPhysicalMeasurements();

    // Visit tracking queries
    @Query("SELECT p FROM Patient p WHERE p.lastVisitDate >= :date")
    List<Patient> findPatientsWithRecentVisit(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Patient p WHERE p.lastVisitDate IS NULL")
    List<Patient> findPatientsWhoNeverVisited();

    @Query("SELECT p FROM Patient p WHERE p.lastVisitDate < :date")
    List<Patient> findPatientsWithOldVisit(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Patient p WHERE p.registrationDate >= :date")
    List<Patient> findPatientsRegisteredAfter(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Patient p WHERE p.registrationDate BETWEEN :startDate AND :endDate")
    List<Patient> findPatientsRegisteredBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Marital status queries
    List<Patient> findByMaritalStatus(Patient.MaritalStatus maritalStatus);

    @Query("SELECT p.maritalStatus, COUNT(p) FROM Patient p WHERE p.maritalStatus IS NOT NULL GROUP BY p.maritalStatus")
    List<Object[]> getPatientCountByMaritalStatus();

    // Search and filter queries
    @Query("SELECT p FROM Patient p WHERE " +
            "(:patientId IS NULL OR p.patientId = :patientId) AND " +
            "(:bloodType IS NULL OR p.bloodType = :bloodType) AND " +
            "(:insuranceProvider IS NULL OR p.insuranceProvider = :insuranceProvider) AND " +
            "(:minAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) >= :minAge) AND " +
            "(:maxAge IS NULL OR YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= :maxAge) AND " +
            "(:active IS NULL OR p.isActivePatient = :active)")
    Page<Patient> findPatientsWithFilters(@Param("patientId") String patientId,
                                          @Param("bloodType") String bloodType,
                                          @Param("insuranceProvider") String insuranceProvider,
                                          @Param("minAge") Integer minAge,
                                          @Param("maxAge") Integer maxAge,
                                          @Param("active") Boolean active,
                                          Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(p.patientId) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Patient> findByNameOrPatientIdOrEmailContaining(@Param("name") String name);

    // Occupation queries
    List<Patient> findByOccupation(String occupation);

    @Query("SELECT DISTINCT p.occupation FROM Patient p WHERE p.occupation IS NOT NULL ORDER BY p.occupation")
    List<String> findAllOccupations();

    // Update queries
    @Modifying
    @Query("UPDATE Patient p SET p.isActivePatient = :active WHERE p.id = :patientId")
    void updateActiveStatus(@Param("patientId") Long patientId, @Param("active") boolean active);

    @Modifying
    @Query("UPDATE Patient p SET p.lastVisitDate = :visitDate WHERE p.id = :patientId")
    void updateLastVisitDate(@Param("patientId") Long patientId, @Param("visitDate") LocalDateTime visitDate);

    @Modifying
    @Query("UPDATE Patient p SET p.heightCm = :height, p.weightKg = :weight WHERE p.id = :patientId")
    void updatePhysicalMeasurements(@Param("patientId") Long patientId,
                                    @Param("height") Double height,
                                    @Param("weight") Double weight);

    @Modifying
    @Query("UPDATE Patient p SET p.insuranceProvider = :provider, p.insuranceNumber = :number WHERE p.id = :patientId")
    void updateInsuranceInfo(@Param("patientId") Long patientId,
                             @Param("provider") String provider,
                             @Param("number") String number);

    @Modifying
    @Query("UPDATE Patient p SET p.emergencyContactName = :name, p.emergencyContactPhone = :phone, p.emergencyContactRelationship = :relationship WHERE p.id = :patientId")
    void updateEmergencyContact(@Param("patientId") Long patientId,
                                @Param("name") String name,
                                @Param("phone") String phone,
                                @Param("relationship") String relationship);

    // Count queries
    long countByIsActivePatientTrue();

    long countByBloodType(String bloodType);

    long countByInsuranceProvider(String insuranceProvider);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.insuranceProvider IS NOT NULL")
    long countPatientsWithInsurance();

    @Query("SELECT COUNT(p) FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) < :age")
    long countPatientsUnderAge(@Param("age") Integer age);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.registrationDate >= :date")
    long countPatientsRegisteredAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.lastVisitDate >= :date")
    long countPatientsWithRecentVisit(@Param("date") LocalDateTime date);

    // Statistical queries
    @Query("SELECT p.bloodType, COUNT(p) FROM Patient p WHERE p.bloodType IS NOT NULL GROUP BY p.bloodType ORDER BY COUNT(p) DESC")
    List<Object[]> getPatientCountByBloodType();

    @Query("SELECT p.insuranceProvider, COUNT(p) FROM Patient p WHERE p.insuranceProvider IS NOT NULL GROUP BY p.insuranceProvider ORDER BY COUNT(p) DESC")
    List<Object[]> getPatientCountByInsuranceProvider();

    @Query("SELECT YEAR(p.dateOfBirth), COUNT(p) FROM Patient p GROUP BY YEAR(p.dateOfBirth) ORDER BY YEAR(p.dateOfBirth) DESC")
    List<Object[]> getPatientCountByBirthYear();

    @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth)) FROM Patient p")
    Double getAveragePatientAge();

    @Query("SELECT AVG(p.heightCm) FROM Patient p WHERE p.heightCm IS NOT NULL")
    Double getAverageHeight();

    @Query("SELECT AVG(p.weightKg) FROM Patient p WHERE p.weightKg IS NOT NULL")
    Double getAverageWeight();

    // With this:
    @Query("SELECT p FROM Patient p WHERE p.id = :userId")
    Optional<Patient> findByUserId(@Param("userId") Long userId);



    // With this (using inherited 'id' field):
    Optional<Patient> findById(Long id);

    @Query("SELECT p FROM Patient p WHERE p.email = :email AND p.isActivePatient  = true")
    Optional<Patient> findActivePatientByEmail(@Param("email") String email);

    // Find by phone (inherited from User)
    Optional<Patient> findByPhone(String phone);


}
