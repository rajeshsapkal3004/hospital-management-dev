package com.hospital_management.repo;


import com.hospital_management.models.Receptionist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {

    // Basic queries
    Optional<Receptionist> findByEmployeeId(String employeeId);

    @Query("SELECT r FROM Receptionist r WHERE r.username = :username")
    Optional<Receptionist> findByUsername(@Param("username") String username);

    @Query("SELECT r FROM Receptionist r WHERE r.email = :email")
    Optional<Receptionist> findByEmail(@Param("email") String email);

    Optional<Receptionist> findByExtensionNumber(String extensionNumber);

    // Existence checks
    boolean existsByEmployeeId(String employeeId);

    boolean existsByExtensionNumber(String extensionNumber);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Receptionist r WHERE r.employeeId = :employeeId AND r.id != :id")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") String employeeId, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Receptionist r WHERE r.extensionNumber = :extensionNumber AND r.id != :id")
    boolean existsByExtensionNumberAndIdNot(@Param("extensionNumber") String extensionNumber, @Param("id") Long id);

    // Department queries
    List<Receptionist> findByDepartment(String department);

    @Query("SELECT DISTINCT r.department FROM Receptionist r WHERE r.department IS NOT NULL ORDER BY r.department")
    List<String> findAllDepartments();

    // Shift queries
    List<Receptionist> findByShift(Receptionist.Shift shift);

    @Query("SELECT r FROM Receptionist r WHERE r.shift = :shift AND r.department = :department")
    List<Receptionist> findByShiftAndDepartment(@Param("shift") Receptionist.Shift shift, @Param("department") String department);

    @Query("SELECT r FROM Receptionist r WHERE r.shiftStartTime <= :currentTime AND r.shiftEndTime >= :currentTime")
    List<Receptionist> findReceptionistsCurrentlyOnDuty(@Param("currentTime") LocalTime currentTime);

    @Query("SELECT r FROM Receptionist r WHERE r.shift = :shift AND r.enabled = true AND r.accountLocked = false")
    List<Receptionist> findActiveReceptionistsByShift(@Param("shift") Receptionist.Shift shift);

    // Desk location queries
    List<Receptionist> findByDeskLocation(String deskLocation);

    @Query("SELECT DISTINCT r.deskLocation FROM Receptionist r WHERE r.deskLocation IS NOT NULL ORDER BY r.deskLocation")
    List<String> findAllDeskLocations();

    @Query("SELECT r FROM Receptionist r WHERE r.deskLocation IS NOT NULL")
    List<Receptionist> findReceptionistsWithAssignedDesks();

    @Query("SELECT r FROM Receptionist r WHERE r.deskLocation IS NULL")
    List<Receptionist> findReceptionistsWithoutAssignedDesks();

    // Permission-based queries
    @Query("SELECT r FROM Receptionist r WHERE r.canHandlePayments = true")
    List<Receptionist> findReceptionistsWhoCanHandlePayments();

    @Query("SELECT r FROM Receptionist r WHERE r.canScheduleAppointments = true")
    List<Receptionist> findReceptionistsWhoCanScheduleAppointments();

    @Query("SELECT r FROM Receptionist r WHERE r.canAccessMedicalRecords = true")
    List<Receptionist> findReceptionistsWhoCanAccessMedicalRecords();

    @Query("SELECT r FROM Receptionist r WHERE r.canHandlePayments = true AND r.canScheduleAppointments = true")
    List<Receptionist> findFullServiceReceptionists();

    // Experience queries
    @Query("SELECT r FROM Receptionist r WHERE r.yearsOfExperience >= :minYears")
    List<Receptionist> findReceptionistsWithMinimumExperience(@Param("minYears") Integer minYears);

    @Query("SELECT r FROM Receptionist r WHERE r.yearsOfExperience BETWEEN :minYears AND :maxYears")
    List<Receptionist> findReceptionistsByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Supervisor queries
    List<Receptionist> findBySupervisorName(String supervisorName);

    @Query("SELECT DISTINCT r.supervisorName FROM Receptionist r WHERE r.supervisorName IS NOT NULL ORDER BY r.supervisorName")
    List<String> findAllSupervisors();

    // Hire date queries
    @Query("SELECT r FROM Receptionist r WHERE r.hireDate >= :date")
    List<Receptionist> findReceptionistsHiredAfter(@Param("date") LocalDate date);

    @Query("SELECT r FROM Receptionist r WHERE r.hireDate BETWEEN :startDate AND :endDate")
    List<Receptionist> findReceptionistsHiredBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Shift time queries
    @Query("SELECT r FROM Receptionist r WHERE r.shiftStartTime = :startTime")
    List<Receptionist> findByShiftStartTime(@Param("startTime") LocalTime startTime);

    @Query("SELECT r FROM Receptionist r WHERE r.shiftEndTime = :endTime")
    List<Receptionist> findByShiftEndTime(@Param("endTime") LocalTime endTime);

    @Query("SELECT r FROM Receptionist r WHERE r.shiftStartTime >= :time OR r.shiftEndTime <= :time")
    List<Receptionist> findReceptionistsNotWorkingAtTime(@Param("time") LocalTime time);

    // Search and filter queries
    @Query("SELECT r FROM Receptionist r WHERE " +
            "(:department IS NULL OR r.department = :department) AND " +
            "(:shift IS NULL OR r.shift = :shift) AND " +
            "(:deskLocation IS NULL OR r.deskLocation = :deskLocation) AND " +
            "(:minExperience IS NULL OR r.yearsOfExperience >= :minExperience) AND " +
            "(:canHandlePayments IS NULL OR r.canHandlePayments = :canHandlePayments) AND " +
            "(:canScheduleAppointments IS NULL OR r.canScheduleAppointments = :canScheduleAppointments) AND " +
            "r.enabled = true")
    Page<Receptionist> findReceptionistsWithFilters(@Param("department") String department,
                                                    @Param("shift") Receptionist.Shift shift,
                                                    @Param("deskLocation") String deskLocation,
                                                    @Param("minExperience") Integer minExperience,
                                                    @Param("canHandlePayments") Boolean canHandlePayments,
                                                    @Param("canScheduleAppointments") Boolean canScheduleAppointments,
                                                    Pageable pageable);

    @Query("SELECT r FROM Receptionist r WHERE " +
            "LOWER(CONCAT(r.firstName, ' ', r.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(r.employeeId) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(r.department) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Receptionist> findByNameOrEmployeeIdOrDepartmentContaining(@Param("name") String name);

    // Active duty queries
    @Query("SELECT r FROM Receptionist r WHERE r.enabled = true AND r.accountLocked = false")
    List<Receptionist> findAllActiveReceptionists();

    @Query("SELECT r FROM Receptionist r WHERE r.enabled = true AND r.accountLocked = false AND r.department = :department")
    List<Receptionist> findActiveReceptionistsByDepartment(@Param("department") String department);

    // Update queries
    @Modifying
    @Query("UPDATE Receptionist r SET r.shift = :shift WHERE r.id = :receptionistId")
    void updateShift(@Param("receptionistId") Long receptionistId, @Param("shift") Receptionist.Shift shift);

    @Modifying
    @Query("UPDATE Receptionist r SET r.shiftStartTime = :startTime, r.shiftEndTime = :endTime WHERE r.id = :receptionistId")
    void updateShiftTimes(@Param("receptionistId") Long receptionistId,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

    @Modifying
    @Query("UPDATE Receptionist r SET r.deskLocation = :deskLocation WHERE r.id = :receptionistId")
    void updateDeskLocation(@Param("receptionistId") Long receptionistId, @Param("deskLocation") String deskLocation);

    @Modifying
    @Query("UPDATE Receptionist r SET r.canHandlePayments = :canHandle WHERE r.id = :receptionistId")
    void updatePaymentHandlingPermission(@Param("receptionistId") Long receptionistId, @Param("canHandle") boolean canHandle);

    @Modifying
    @Query("UPDATE Receptionist r SET r.canScheduleAppointments = :canSchedule WHERE r.id = :receptionistId")
    void updateAppointmentSchedulingPermission(@Param("receptionistId") Long receptionistId, @Param("canSchedule") boolean canSchedule);

    @Modifying
    @Query("UPDATE Receptionist r SET r.canAccessMedicalRecords = :canAccess WHERE r.id = :receptionistId")
    void updateMedicalRecordAccessPermission(@Param("receptionistId") Long receptionistId, @Param("canAccess") boolean canAccess);

    @Modifying
    @Query("UPDATE Receptionist r SET r.extensionNumber = :extensionNumber WHERE r.id = :receptionistId")
    void updateExtensionNumber(@Param("receptionistId") Long receptionistId, @Param("extensionNumber") String extensionNumber);

    // Count queries
    long countByDepartment(String department);

    long countByShift(Receptionist.Shift shift);

    @Query("SELECT COUNT(r) FROM Receptionist r WHERE r.canHandlePayments = true")
    long countReceptionistsWhoCanHandlePayments();

    @Query("SELECT COUNT(r) FROM Receptionist r WHERE r.canScheduleAppointments = true")
    long countReceptionistsWhoCanScheduleAppointments();

    @Query("SELECT COUNT(r) FROM Receptionist r WHERE r.canAccessMedicalRecords = true")
    long countReceptionistsWhoCanAccessMedicalRecords();

    @Query("SELECT COUNT(r) FROM Receptionist r WHERE r.enabled = true AND r.accountLocked = false")
    long countActiveReceptionists();

    @Query("SELECT COUNT(r) FROM Receptionist r WHERE r.deskLocation IS NOT NULL")
    long countReceptionistsWithAssignedDesks();

    // Statistical queries
    @Query("SELECT r.department, COUNT(r) FROM Receptionist r WHERE r.department IS NOT NULL GROUP BY r.department ORDER BY COUNT(r) DESC")
    List<Object[]> getReceptionistCountByDepartment();

    @Query("SELECT r.shift, COUNT(r) FROM Receptionist r WHERE r.shift IS NOT NULL GROUP BY r.shift ORDER BY COUNT(r) DESC")
    List<Object[]> getReceptionistCountByShift();

    @Query("SELECT r.deskLocation, COUNT(r) FROM Receptionist r WHERE r.deskLocation IS NOT NULL GROUP BY r.deskLocation ORDER BY COUNT(r) DESC")
    List<Object[]> getReceptionistCountByDeskLocation();

    @Query("SELECT AVG(r.yearsOfExperience) FROM Receptionist r WHERE r.yearsOfExperience IS NOT NULL")
    Double getAverageExperience();

    // Coverage queries
    @Query("SELECT COUNT(r) FROM Receptionist r WHERE r.shift = :shift AND r.department = :department AND r.enabled = true")
    long countActiveReceptionistsByShiftAndDepartment(@Param("shift") Receptionist.Shift shift, @Param("department") String department);

    @Query("SELECT r.department, r.shift, COUNT(r) FROM Receptionist r WHERE r.enabled = true GROUP BY r.department, r.shift ORDER BY r.department, r.shift")
    List<Object[]> getReceptionistCoverageByDepartmentAndShift();
}
