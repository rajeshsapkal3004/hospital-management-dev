package com.hospital_management.repo;


import com.hospital_management.models.LabTechnician;
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
public interface LabTechnicianRepository extends JpaRepository<LabTechnician, Long> {

    // Basic queries
    Optional<LabTechnician> findByEmployeeId(String employeeId);

    Optional<LabTechnician> findByCertificationNumber(String certificationNumber);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.username = :username")
    Optional<LabTechnician> findByUsername(@Param("username") String username);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.email = :email")
    Optional<LabTechnician> findByEmail(@Param("email") String email);

    // Existence checks
    boolean existsByEmployeeId(String employeeId);

    boolean existsByCertificationNumber(String certificationNumber);

    @Query("SELECT CASE WHEN COUNT(lt) > 0 THEN true ELSE false END FROM LabTechnician lt WHERE lt.employeeId = :employeeId AND lt.id != :id")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") String employeeId, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(lt) > 0 THEN true ELSE false END FROM LabTechnician lt WHERE lt.certificationNumber = :certificationNumber AND lt.id != :id")
    boolean existsByCertificationNumberAndIdNot(@Param("certificationNumber") String certificationNumber, @Param("id") Long id);

    // Department and section queries
    List<LabTechnician> findByDepartment(String department);

    List<LabTechnician> findByLabSection(String labSection);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.department = :department AND lt.labSection = :section")
    List<LabTechnician> findByDepartmentAndSection(@Param("department") String department, @Param("section") String section);

    @Query("SELECT DISTINCT lt.department FROM LabTechnician lt WHERE lt.department IS NOT NULL ORDER BY lt.department")
    List<String> findAllDepartments();

    @Query("SELECT DISTINCT lt.labSection FROM LabTechnician lt WHERE lt.labSection IS NOT NULL ORDER BY lt.labSection")
    List<String> findAllLabSections();

    // Shift queries
    List<LabTechnician> findByShift(LabTechnician.Shift shift);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.shift = :shift AND lt.department = :department")
    List<LabTechnician> findByShiftAndDepartment(@Param("shift") LabTechnician.Shift shift, @Param("department") String department);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.shift = :shift AND lt.labSection = :section")
    List<LabTechnician> findByShiftAndSection(@Param("shift") LabTechnician.Shift shift, @Param("section") String section);

    // Status and permission queries
    @Query("SELECT lt FROM LabTechnician lt WHERE lt.canApproveResults = true")
    List<LabTechnician> findTechniciansWhoCanApproveResults();

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.canOperateEquipment = true")
    List<LabTechnician> findTechniciansWhoCanOperateEquipment();

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.isSeniorTechnician = true")
    List<LabTechnician> findSeniorTechnicians();

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.canApproveResults = true AND lt.department = :department")
    List<LabTechnician> findApprovalAuthorizedTechniciansByDepartment(@Param("department") String department);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.isSeniorTechnician = true AND lt.labSection = :section")
    List<LabTechnician> findSeniorTechniciansBySection(@Param("section") String section);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.enabled = true AND lt.accountLocked = false")
    List<LabTechnician> findAllActiveTechnicians();

    // Certification queries
    @Query("SELECT lt FROM LabTechnician lt WHERE lt.certificationExpiry IS NULL OR lt.certificationExpiry > CURRENT_DATE")
    List<LabTechnician> findTechniciansWithValidCertification();

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.certificationExpiry IS NOT NULL AND lt.certificationExpiry <= :date")
    List<LabTechnician> findTechniciansWithExpiringCertification(@Param("date") LocalDate date);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.certificationExpiry IS NOT NULL AND lt.certificationExpiry < CURRENT_DATE")
    List<LabTechnician> findTechniciansWithExpiredCertification();

    // Experience queries
    @Query("SELECT lt FROM LabTechnician lt WHERE lt.yearsOfExperience >= :minYears")
    List<LabTechnician> findTechniciansWithMinimumExperience(@Param("minYears") Integer minYears);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.yearsOfExperience BETWEEN :minYears AND :maxYears")
    List<LabTechnician> findTechniciansByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Supervisor queries
    List<LabTechnician> findBySupervisorName(String supervisorName);

    @Query("SELECT DISTINCT lt.supervisorName FROM LabTechnician lt WHERE lt.supervisorName IS NOT NULL ORDER BY lt.supervisorName")
    List<String> findAllSupervisors();

    // Specialization queries
    @Query("SELECT lt FROM LabTechnician lt JOIN lt.specializations s WHERE s = :specialization")
    List<LabTechnician> findBySpecialization(@Param("specialization") String specialization);

    @Query("SELECT lt FROM LabTechnician lt WHERE SIZE(lt.specializations) > 0")
    List<LabTechnician> findTechniciansWithSpecializations();

    @Query("SELECT lt FROM LabTechnician lt WHERE SIZE(lt.specializations) >= :minCount")
    List<LabTechnician> findTechniciansWithMinSpecializations(@Param("minCount") int minCount);

    // Equipment queries
    @Query("SELECT lt FROM LabTechnician lt JOIN lt.certifiedEquipment e WHERE e = :equipment")
    List<LabTechnician> findByCertifiedEquipment(@Param("equipment") String equipment);

    @Query("SELECT lt FROM LabTechnician lt WHERE SIZE(lt.certifiedEquipment) > 0")
    List<LabTechnician> findTechniciansWithCertifiedEquipment();

    @Query("SELECT lt FROM LabTechnician lt WHERE SIZE(lt.certifiedEquipment) >= :minCount")
    List<LabTechnician> findTechniciansWithMinEquipmentCertifications(@Param("minCount") int minCount);

    // Hire date queries
    @Query("SELECT lt FROM LabTechnician lt WHERE lt.hireDate >= :date")
    List<LabTechnician> findTechniciansHiredAfter(@Param("date") LocalDate date);

    @Query("SELECT lt FROM LabTechnician lt WHERE lt.hireDate BETWEEN :startDate AND :endDate")
    List<LabTechnician> findTechniciansHiredBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Search and filter queries
    @Query("SELECT lt FROM LabTechnician lt WHERE " +
            "(:department IS NULL OR lt.department = :department) AND " +
            "(:labSection IS NULL OR lt.labSection = :labSection) AND " +
            "(:shift IS NULL OR lt.shift = :shift) AND " +
            "(:minExperience IS NULL OR lt.yearsOfExperience >= :minExperience) AND " +
            "(:canApproveResults IS NULL OR lt.canApproveResults = :canApproveResults) AND " +
            "(:isSeniorTechnician IS NULL OR lt.isSeniorTechnician = :isSeniorTechnician) AND " +
            "lt.enabled = true")
    Page<LabTechnician> findTechniciansWithFilters(@Param("department") String department,
                                                   @Param("labSection") String labSection,
                                                   @Param("shift") LabTechnician.Shift shift,
                                                   @Param("minExperience") Integer minExperience,
                                                   @Param("canApproveResults") Boolean canApproveResults,
                                                   @Param("isSeniorTechnician") Boolean isSeniorTechnician,
                                                   Pageable pageable);

    @Query("SELECT lt FROM LabTechnician lt WHERE " +
            "LOWER(CONCAT(lt.firstName, ' ', lt.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(lt.employeeId) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(lt.department) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(lt.labSection) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<LabTechnician> findByNameOrEmployeeIdOrDepartmentOrSectionContaining(@Param("name") String name);

    // Update queries
    @Modifying
    @Query("UPDATE LabTechnician lt SET lt.canApproveResults = :canApprove WHERE lt.id = :technicianId")
    void updateResultApprovalPermission(@Param("technicianId") Long technicianId, @Param("canApprove") boolean canApprove);

    @Modifying
    @Query("UPDATE LabTechnician lt SET lt.canOperateEquipment = :canOperate WHERE lt.id = :technicianId")
    void updateEquipmentOperationPermission(@Param("technicianId") Long technicianId, @Param("canOperate") boolean canOperate);

    @Modifying
    @Query("UPDATE LabTechnician lt SET lt.isSeniorTechnician = :isSenior WHERE lt.id = :technicianId")
    void updateSeniorTechnicianStatus(@Param("technicianId") Long technicianId, @Param("isSenior") boolean isSenior);

    @Modifying
    @Query("UPDATE LabTechnician lt SET lt.shift = :shift WHERE lt.id = :technicianId")
    void updateShift(@Param("technicianId") Long technicianId, @Param("shift") LabTechnician.Shift shift);

    @Modifying
    @Query("UPDATE LabTechnician lt SET lt.labSection = :section WHERE lt.id = :technicianId")
    void updateLabSection(@Param("technicianId") Long technicianId, @Param("section") String section);

    // Count queries
    long countByDepartment(String department);

    long countByLabSection(String labSection);

    long countByShift(LabTechnician.Shift shift);

    @Query("SELECT COUNT(lt) FROM LabTechnician lt WHERE lt.canApproveResults = true")
    long countTechniciansWhoCanApproveResults();

    @Query("SELECT COUNT(lt) FROM LabTechnician lt WHERE lt.isSeniorTechnician = true")
    long countSeniorTechnicians();

    @Query("SELECT COUNT(lt) FROM LabTechnician lt WHERE lt.enabled = true AND lt.accountLocked = false")
    long countActiveTechnicians();

    @Query("SELECT COUNT(lt) FROM LabTechnician lt WHERE lt.certificationExpiry IS NOT NULL AND lt.certificationExpiry <= :date")
    long countTechniciansWithExpiringCertification(@Param("date") LocalDate date);

    // Statistical queries
    @Query("SELECT lt.department, COUNT(lt) FROM LabTechnician lt WHERE lt.department IS NOT NULL GROUP BY lt.department ORDER BY COUNT(lt) DESC")
    List<Object[]> getTechnicianCountByDepartment();

    @Query("SELECT lt.labSection, COUNT(lt) FROM LabTechnician lt WHERE lt.labSection IS NOT NULL GROUP BY lt.labSection ORDER BY COUNT(lt) DESC")
    List<Object[]> getTechnicianCountBySection();

    @Query("SELECT lt.shift, COUNT(lt) FROM LabTechnician lt WHERE lt.shift IS NOT NULL GROUP BY lt.shift ORDER BY COUNT(lt) DESC")
    List<Object[]> getTechnicianCountByShift();

    @Query("SELECT AVG(lt.yearsOfExperience) FROM LabTechnician lt WHERE lt.yearsOfExperience IS NOT NULL")
    Double getAverageExperience();

    // Equipment and specialization statistics
    @Query("SELECT s, COUNT(lt) FROM LabTechnician lt JOIN lt.specializations s GROUP BY s ORDER BY COUNT(lt) DESC")
    List<Object[]> getTechnicianCountBySpecialization();

    @Query("SELECT e, COUNT(lt) FROM LabTechnician lt JOIN lt.certifiedEquipment e GROUP BY e ORDER BY COUNT(lt) DESC")
    List<Object[]> getTechnicianCountByCertifiedEquipment();

    // Coverage queries
    @Query("SELECT COUNT(lt) FROM LabTechnician lt WHERE lt.shift = :shift AND lt.department = :department AND lt.enabled = true")
    long countActiveTechniciansByShiftAndDepartment(@Param("shift") LabTechnician.Shift shift, @Param("department") String department);

    @Query("SELECT lt.department, lt.shift, COUNT(lt) FROM LabTechnician lt WHERE lt.enabled = true GROUP BY lt.department, lt.shift ORDER BY lt.department, lt.shift")
    List<Object[]> getTechnicianCoverageByDepartmentAndShift();
}
