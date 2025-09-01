package com.hospital_management.repo;

import com.hospital_management.models.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Basic queries
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    Optional<Doctor> findByNpiNumber(String npiNumber);

    Optional<Doctor> findByEmployeeId(String employeeId);

    @Query("SELECT d FROM Doctor d WHERE d.username = :username")
    Optional<Doctor> findByUsername(@Param("username") String username);

    @Query("SELECT d FROM Doctor d WHERE d.email = :email")
    Optional<Doctor> findByEmail(@Param("email") String email);

    // Existence checks
    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByNpiNumber(String npiNumber);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Doctor d WHERE d.licenseNumber = :licenseNumber AND d.id != :id")
    boolean existsByLicenseNumberAndIdNot(@Param("licenseNumber") String licenseNumber, @Param("id") Long id);

    // Specialization and department queries
    List<Doctor> findBySpecialization(String specialization);

    List<Doctor> findByDepartment(String department);

    @Query("SELECT d FROM Doctor d WHERE d.specialization = :specialization AND d.department = :department")
    List<Doctor> findBySpecializationAndDepartment(@Param("specialization") String specialization,
                                                   @Param("department") String department);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.specialization IS NOT NULL ORDER BY d.specialization")
    List<String> findAllSpecializations();

    @Query("SELECT DISTINCT d.department FROM Doctor d WHERE d.department IS NOT NULL ORDER BY d.department")
    List<String> findAllDepartments();

    // Availability queries
    @Query("SELECT d FROM Doctor d WHERE d.isAvailableForConsultation = true AND d.enabled = true AND d.accountLocked = false")
    List<Doctor> findAvailableDoctors();

    @Query("SELECT d FROM Doctor d WHERE d.isAvailableForConsultation = true AND d.enabled = true AND d.accountLocked = false AND d.specialization = :specialization")
    List<Doctor> findAvailableDoctorsBySpecialization(@Param("specialization") String specialization);

    @Query("SELECT d FROM Doctor d WHERE d.isAvailableForConsultation = true AND d.enabled = true AND d.accountLocked = false AND d.department = :department")
    List<Doctor> findAvailableDoctorsByDepartment(@Param("department") String department);

    @Query("SELECT d FROM Doctor d WHERE d.isEmergencyContact = true AND d.enabled = true AND d.accountLocked = false")
    List<Doctor> findEmergencyContactDoctors();

    // License and certification queries
    @Query("SELECT d FROM Doctor d WHERE d.licenseExpiry IS NULL OR d.licenseExpiry > CURRENT_DATE")
    List<Doctor> findDoctorsWithValidLicense();

    @Query("SELECT d FROM Doctor d WHERE d.licenseExpiry IS NOT NULL AND d.licenseExpiry <= :date")
    List<Doctor> findDoctorsWithExpiringLicense(@Param("date") LocalDate date);

    @Query("SELECT d FROM Doctor d WHERE d.licenseExpiry IS NOT NULL AND d.licenseExpiry < CURRENT_DATE")
    List<Doctor> findDoctorsWithExpiredLicense();

    // Experience and qualification queries
    @Query("SELECT d FROM Doctor d WHERE d.yearsOfExperience >= :minYears")
    List<Doctor> findDoctorsWithMinimumExperience(@Param("minYears") Integer minYears);

    @Query("SELECT d FROM Doctor d WHERE d.yearsOfExperience BETWEEN :minYears AND :maxYears")
    List<Doctor> findDoctorsByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Fee-based queries
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee <= :maxFee")
    List<Doctor> findDoctorsWithMaxFee(@Param("maxFee") BigDecimal maxFee);

    @Query("SELECT d FROM Doctor d WHERE d.consultationFee BETWEEN :minFee AND :maxFee")
    List<Doctor> findDoctorsByFeeRange(@Param("minFee") BigDecimal minFee, @Param("maxFee") BigDecimal maxFee);

    // Search and filter queries
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:specialization IS NULL OR d.specialization = :specialization) AND " +
            "(:department IS NULL OR d.department = :department) AND " +
            "(:minExperience IS NULL OR d.yearsOfExperience >= :minExperience) AND " +
            "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
            "(:available IS NULL OR d.isAvailableForConsultation = :available) AND " +
            "d.enabled = true")
    Page<Doctor> findDoctorsWithFilters(@Param("specialization") String specialization,
                                        @Param("department") String department,
                                        @Param("minExperience") Integer minExperience,
                                        @Param("maxFee") BigDecimal maxFee,
                                        @Param("available") Boolean available,
                                        Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE " +
            "LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(d.department) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> findByNameOrSpecializationOrDepartmentContaining(@Param("name") String name);

    // Room and location queries
    List<Doctor> findByRoomNumber(String roomNumber);

    @Query("SELECT d FROM Doctor d WHERE d.roomNumber IS NOT NULL")
    List<Doctor> findDoctorsWithAssignedRooms();

    @Query("SELECT d FROM Doctor d WHERE d.roomNumber IS NULL")
    List<Doctor> findDoctorsWithoutAssignedRooms();

    // Hire date queries
    @Query("SELECT d FROM Doctor d WHERE d.hireDate >= :date")
    List<Doctor> findDoctorsHiredAfter(@Param("date") LocalDate date);

    @Query("SELECT d FROM Doctor d WHERE d.hireDate BETWEEN :startDate AND :endDate")
    List<Doctor> findDoctorsHiredBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Update queries
    @Modifying
    @Query("UPDATE Doctor d SET d.isAvailableForConsultation = :available WHERE d.id = :doctorId")
    void updateAvailabilityStatus(@Param("doctorId") Long doctorId, @Param("available") boolean available);

    @Modifying
    @Query("UPDATE Doctor d SET d.consultationFee = :fee WHERE d.id = :doctorId")
    void updateConsultationFee(@Param("doctorId") Long doctorId, @Param("fee") BigDecimal fee);

    @Modifying
    @Query("UPDATE Doctor d SET d.roomNumber = :roomNumber WHERE d.id = :doctorId")
    void updateRoomNumber(@Param("doctorId") Long doctorId, @Param("roomNumber") String roomNumber);

    @Modifying
    @Query("UPDATE Doctor d SET d.isEmergencyContact = :emergency WHERE d.id = :doctorId")
    void updateEmergencyContactStatus(@Param("doctorId") Long doctorId, @Param("emergency") boolean emergency);

    // Count queries
    long countBySpecialization(String specialization);

    long countByDepartment(String department);

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.isAvailableForConsultation = true AND d.enabled = true")
    long countAvailableDoctors();

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.isEmergencyContact = true")
    long countEmergencyContactDoctors();

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.licenseExpiry IS NOT NULL AND d.licenseExpiry <= :date")
    long countDoctorsWithExpiringLicense(@Param("date") LocalDate date);

    // Statistical queries
    @Query("SELECT d.specialization, COUNT(d) FROM Doctor d WHERE d.specialization IS NOT NULL GROUP BY d.specialization ORDER BY COUNT(d) DESC")
    List<Object[]> getDoctorCountBySpecialization();

    @Query("SELECT d.department, COUNT(d) FROM Doctor d WHERE d.department IS NOT NULL GROUP BY d.department ORDER BY COUNT(d) DESC")
    List<Object[]> getDoctorCountByDepartment();

    @Query("SELECT AVG(d.consultationFee) FROM Doctor d WHERE d.consultationFee IS NOT NULL")
    BigDecimal getAverageConsultationFee();

    @Query("SELECT d.specialization, AVG(d.consultationFee) FROM Doctor d WHERE d.consultationFee IS NOT NULL AND d.specialization IS NOT NULL GROUP BY d.specialization")
    List<Object[]> getAverageConsultationFeeBySpecialization();
}
