package com.hospital_management.repo;

import com.hospital_management.models.Nurse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long> {

    // Basic queries
    Optional<Nurse> findByLicenseNumber(String licenseNumber);

    Optional<Nurse> findByEmployeeId(String employeeId);

    @Query("SELECT n FROM Nurse n WHERE n.username = :username")
    Optional<Nurse> findByUsername(@Param("username") String username);

    @Query("SELECT n FROM Nurse n WHERE n.email = :email")
    Optional<Nurse> findByEmail(@Param("email") String email);

    // Existence checks
    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Nurse n WHERE n.licenseNumber = :licenseNumber AND n.id != :id")
    boolean existsByLicenseNumberAndIdNot(@Param("licenseNumber") String licenseNumber, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Nurse n WHERE n.employeeId = :employeeId AND n.id != :id")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") String employeeId, @Param("id") Long id);

    // Department and ward queries
    List<Nurse> findByDepartment(String department);

    List<Nurse> findByWardAssignment(String wardAssignment);

    @Query("SELECT n FROM Nurse n WHERE n.department = :department AND n.wardAssignment = :ward")
    List<Nurse> findByDepartmentAndWard(@Param("department") String department, @Param("ward") String ward);

    @Query("SELECT DISTINCT n.department FROM Nurse n WHERE n.department IS NOT NULL ORDER BY n.department")
    List<String> findAllDepartments();

    @Query("SELECT DISTINCT n.wardAssignment FROM Nurse n WHERE n.wardAssignment IS NOT NULL ORDER BY n.wardAssignment")
    List<String> findAllWardAssignments();

    // Shift queries
    List<Nurse> findByShift(Nurse.Shift shift);

    @Query("SELECT n FROM Nurse n WHERE n.shift = :shift AND n.department = :department")
    List<Nurse> findByShiftAndDepartment(@Param("shift") Nurse.Shift shift, @Param("department") String department);

    @Query("SELECT n FROM Nurse n WHERE n.shift = :shift AND n.wardAssignment = :ward")
    List<Nurse> findByShiftAndWard(@Param("shift") Nurse.Shift shift, @Param("ward") String ward);

    // Nurse type queries
    List<Nurse> findByNurseType(Nurse.NurseType nurseType);

    @Query("SELECT n FROM Nurse n WHERE n.nurseType = :nurseType AND n.department = :department")
    List<Nurse> findByNurseTypeAndDepartment(@Param("nurseType") Nurse.NurseType nurseType, @Param("department") String department);

    // Availability and status queries
    @Query("SELECT n FROM Nurse n WHERE n.isAvailableForDuty = true AND n.enabled = true AND n.accountLocked = false")
    List<Nurse> findAvailableNurses();

    @Query("SELECT n FROM Nurse n WHERE n.isAvailableForDuty = true AND n.enabled = true AND n.accountLocked = false AND n.shift = :shift")
    List<Nurse> findAvailableNursesByShift(@Param("shift") Nurse.Shift shift);

    @Query("SELECT n FROM Nurse n WHERE n.isAvailableForDuty = true AND n.enabled = true AND n.accountLocked = false AND n.department = :department")
    List<Nurse> findAvailableNursesByDepartment(@Param("department") String department);

    @Query("SELECT n FROM Nurse n WHERE n.isHeadNurse = true")
    List<Nurse> findHeadNurses();

    @Query("SELECT n FROM Nurse n WHERE n.isHeadNurse = true AND n.department = :department")
    List<Nurse> findHeadNursesByDepartment(@Param("department") String department);

    @Query("SELECT n FROM Nurse n WHERE n.canAdministerMedication = true")
    List<Nurse> findNursesWhoCanAdministerMedication();

    // License and certification queries
    @Query("SELECT n FROM Nurse n WHERE n.certificationExpiry IS NULL OR n.certificationExpiry > CURRENT_DATE")
    List<Nurse> findNursesWithValidCertification();

    @Query("SELECT n FROM Nurse n WHERE n.certificationExpiry IS NOT NULL AND n.certificationExpiry <= :date")
    List<Nurse> findNursesWithExpiringCertification(@Param("date") LocalDate date);

    @Query("SELECT n FROM Nurse n WHERE n.certificationExpiry IS NOT NULL AND n.certificationExpiry < CURRENT_DATE")
    List<Nurse> findNursesWithExpiredCertification();

    // Experience queries
    @Query("SELECT n FROM Nurse n WHERE n.yearsOfExperience >= :minYears")
    List<Nurse> findNursesWithMinimumExperience(@Param("minYears") Integer minYears);

    @Query("SELECT n FROM Nurse n WHERE n.yearsOfExperience BETWEEN :minYears AND :maxYears")
    List<Nurse> findNursesByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Supervisor queries
    List<Nurse> findBySupervisorName(String supervisorName);

    @Query("SELECT DISTINCT n.supervisorName FROM Nurse n WHERE n.supervisorName IS NOT NULL ORDER BY n.supervisorName")
    List<String> findAllSupervisors();

    // Specialization queries
    @Query("SELECT n FROM Nurse n JOIN n.specializations s WHERE s = :specialization")
    List<Nurse> findBySpecialization(@Param("specialization") String specialization);

    @Query("SELECT n FROM Nurse n WHERE SIZE(n.specializations) > 0")
    List<Nurse> findNursesWithSpecializations();

    // Hire date queries
    @Query("SELECT n FROM Nurse n WHERE n.hireDate >= :date")
    List<Nurse> findNursesHiredAfter(@Param("date") LocalDate date);

    @Query("SELECT n FROM Nurse n WHERE n.hireDate BETWEEN :startDate AND :endDate")
    List<Nurse> findNursesHiredBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Search and filter queries
    @Query("SELECT n FROM Nurse n WHERE " +
            "(:department IS NULL OR n.department = :department) AND " +
            "(:shift IS NULL OR n.shift = :shift) AND " +
            "(:nurseType IS NULL OR n.nurseType = :nurseType) AND " +
            "(:wardAssignment IS NULL OR n.wardAssignment = :wardAssignment) AND " +
            "(:minExperience IS NULL OR n.yearsOfExperience >= :minExperience) AND " +
            "(:available IS NULL OR n.isAvailableForDuty = :available) AND " +
            "(:headNurse IS NULL OR n.isHeadNurse = :headNurse) AND " +
            "n.enabled = true")
    Page<Nurse> findNursesWithFilters(@Param("department") String department,
                                      @Param("shift") Nurse.Shift shift,
                                      @Param("nurseType") Nurse.NurseType nurseType,
                                      @Param("wardAssignment") String wardAssignment,
                                      @Param("minExperience") Integer minExperience,
                                      @Param("available") Boolean available,
                                      @Param("headNurse") Boolean headNurse,
                                      Pageable pageable);

    @Query("SELECT n FROM Nurse n WHERE " +
            "LOWER(CONCAT(n.firstName, ' ', n.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(n.employeeId) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(n.department) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Nurse> findByNameOrEmployeeIdOrDepartmentContaining(@Param("name") String name);

    // Update queries
    @Modifying
    @Query("UPDATE Nurse n SET n.isAvailableForDuty = :available WHERE n.id = :nurseId")
    void updateAvailabilityStatus(@Param("nurseId") Long nurseId, @Param("available") boolean available);

    @Modifying
    @Query("UPDATE Nurse n SET n.shift = :shift WHERE n.id = :nurseId")
    void updateShift(@Param("nurseId") Long nurseId, @Param("shift") Nurse.Shift shift);

    @Modifying
    @Query("UPDATE Nurse n SET n.wardAssignment = :ward WHERE n.id = :nurseId")
    void updateWardAssignment(@Param("nurseId") Long nurseId, @Param("ward") String ward);

    @Modifying
    @Query("UPDATE Nurse n SET n.isHeadNurse = :headNurse WHERE n.id = :nurseId")
    void updateHeadNurseStatus(@Param("nurseId") Long nurseId, @Param("headNurse") boolean headNurse);

    @Modifying
    @Query("UPDATE Nurse n SET n.canAdministerMedication = :canAdminister WHERE n.id = :nurseId")
    void updateMedicationAdministrationPermission(@Param("nurseId") Long nurseId, @Param("canAdminister") boolean canAdminister);

    // Count queries
    long countByDepartment(String department);

    long countByShift(Nurse.Shift shift);

    long countByNurseType(Nurse.NurseType nurseType);

    @Query("SELECT COUNT(n) FROM Nurse n WHERE n.isAvailableForDuty = true AND n.enabled = true")
    long countAvailableNurses();

    @Query("SELECT COUNT(n) FROM Nurse n WHERE n.isHeadNurse = true")
    long countHeadNurses();

    @Query("SELECT COUNT(n) FROM Nurse n WHERE n.canAdministerMedication = true")
    long countNursesWhoCanAdministerMedication();

    @Query("SELECT COUNT(n) FROM Nurse n WHERE n.certificationExpiry IS NOT NULL AND n.certificationExpiry <= :date")
    long countNursesWithExpiringCertification(@Param("date") LocalDate date);

    // Statistical queries
    @Query("SELECT n.department, COUNT(n) FROM Nurse n WHERE n.department IS NOT NULL GROUP BY n.department ORDER BY COUNT(n) DESC")
    List<Object[]> getNurseCountByDepartment();

    @Query("SELECT n.shift, COUNT(n) FROM Nurse n WHERE n.shift IS NOT NULL GROUP BY n.shift ORDER BY COUNT(n) DESC")
    List<Object[]> getNurseCountByShift();

    @Query("SELECT n.nurseType, COUNT(n) FROM Nurse n WHERE n.nurseType IS NOT NULL GROUP BY n.nurseType ORDER BY COUNT(n) DESC")
    List<Object[]> getNurseCountByType();

    @Query("SELECT n.wardAssignment, COUNT(n) FROM Nurse n WHERE n.wardAssignment IS NOT NULL GROUP BY n.wardAssignment ORDER BY COUNT(n) DESC")
    List<Object[]> getNurseCountByWard();

    @Query("SELECT AVG(n.yearsOfExperience) FROM Nurse n WHERE n.yearsOfExperience IS NOT NULL")
    Double getAverageExperience();
}
