package com.hospital_management.repo;


import com.hospital_management.models.Admin;
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
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Basic queries
    Optional<Admin> findByEmployeeId(String employeeId);

    @Query("SELECT a FROM Admin a WHERE a.username = :username")
    Optional<Admin> findByUsername(@Param("username") String username);

    @Query("SELECT a FROM Admin a WHERE a.email = :email")
    Optional<Admin> findByEmail(@Param("email") String email);

    Optional<Admin> findByExtensionNumber(String extensionNumber);

    // Existence checks
    boolean existsByEmployeeId(String employeeId);

    boolean existsByExtensionNumber(String extensionNumber);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.employeeId = :employeeId AND a.id != :id")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") String employeeId, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.extensionNumber = :extensionNumber AND a.id != :id")
    boolean existsByExtensionNumberAndIdNot(@Param("extensionNumber") String extensionNumber, @Param("id") Long id);

    // Department queries
    List<Admin> findByDepartment(String department);

    @Query("SELECT DISTINCT a.department FROM Admin a WHERE a.department IS NOT NULL ORDER BY a.department")
    List<String> findAllDepartments();

    @Query("SELECT a FROM Admin a JOIN a.managedDepartments md WHERE md = :department")
    List<Admin> findByManagedDepartment(@Param("department") String department);

    // Admin level queries
    List<Admin> findByAdminLevel(Admin.AdminLevel adminLevel);

    @Query("SELECT a FROM Admin a WHERE a.adminLevel = :adminLevel AND a.department = :department")
    List<Admin> findByAdminLevelAndDepartment(@Param("adminLevel") Admin.AdminLevel adminLevel, @Param("department") String department);

    // Job title and hierarchy queries
    List<Admin> findByJobTitle(String jobTitle);

    List<Admin> findByReportsTo(String reportsTo);

    @Query("SELECT DISTINCT a.jobTitle FROM Admin a WHERE a.jobTitle IS NOT NULL ORDER BY a.jobTitle")
    List<String> findAllJobTitles();

    @Query("SELECT DISTINCT a.reportsTo FROM Admin a WHERE a.reportsTo IS NOT NULL ORDER BY a.reportsTo")
    List<String> findAllSupervisors();

    // Permission-based queries
    @Query("SELECT a FROM Admin a WHERE a.canManageUsers = true")
    List<Admin> findAdminsWhoCanManageUsers();

    @Query("SELECT a FROM Admin a WHERE a.canAccessFinancialData = true")
    List<Admin> findAdminsWhoCanAccessFinancialData();

    @Query("SELECT a FROM Admin a WHERE a.canGenerateReports = true")
    List<Admin> findAdminsWhoCanGenerateReports();

    @Query("SELECT a FROM Admin a WHERE a.canModifySystemSettings = true")
    List<Admin> findAdminsWhoCanModifySystemSettings();

    @Query("SELECT a FROM Admin a WHERE a.isSuperAdmin = true")
    List<Admin> findSuperAdmins();

    @Query("SELECT a FROM Admin a WHERE a.canManageUsers = true AND a.canAccessFinancialData = true AND a.canGenerateReports = true")
    List<Admin> findFullPrivilegeAdmins();

    // Experience queries
    @Query("SELECT a FROM Admin a WHERE a.yearsOfExperience >= :minYears")
    List<Admin> findAdminsWithMinimumExperience(@Param("minYears") Integer minYears);

    @Query("SELECT a FROM Admin a WHERE a.yearsOfExperience BETWEEN :minYears AND :maxYears")
    List<Admin> findAdminsByExperienceRange(@Param("minYears") Integer minYears, @Param("maxYears") Integer maxYears);

    // Office location queries
    List<Admin> findByOfficeLocation(String officeLocation);

    @Query("SELECT DISTINCT a.officeLocation FROM Admin a WHERE a.officeLocation IS NOT NULL ORDER BY a.officeLocation")
    List<String> findAllOfficeLocations();

    @Query("SELECT a FROM Admin a WHERE a.officeLocation IS NOT NULL")
    List<Admin> findAdminsWithAssignedOffices();

    @Query("SELECT a FROM Admin a WHERE a.officeLocation IS NULL")
    List<Admin> findAdminsWithoutAssignedOffices();

    // Hire date queries
    @Query("SELECT a FROM Admin a WHERE a.hireDate >= :date")
    List<Admin> findAdminsHiredAfter(@Param("date") LocalDate date);

    @Query("SELECT a FROM Admin a WHERE a.hireDate BETWEEN :startDate AND :endDate")
    List<Admin> findAdminsHiredBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Active status queries
    @Query("SELECT a FROM Admin a WHERE a.enabled = true AND a.accountLocked = false")
    List<Admin> findAllActiveAdmins();

    @Query("SELECT a FROM Admin a WHERE a.enabled = true AND a.accountLocked = false AND a.department = :department")
    List<Admin> findActiveAdminsByDepartment(@Param("department") String department);

    @Query("SELECT a FROM Admin a WHERE a.enabled = true AND a.accountLocked = false AND a.adminLevel = :adminLevel")
    List<Admin> findActiveAdminsByLevel(@Param("adminLevel") Admin.AdminLevel adminLevel);

    // Search and filter queries
    @Query("SELECT a FROM Admin a WHERE " +
            "(:department IS NULL OR a.department = :department) AND " +
            "(:adminLevel IS NULL OR a.adminLevel = :adminLevel) AND " +
            "(:jobTitle IS NULL OR LOWER(a.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
            "(:minExperience IS NULL OR a.yearsOfExperience >= :minExperience) AND " +
            "(:canManageUsers IS NULL OR a.canManageUsers = :canManageUsers) AND " +
            "(:canAccessFinancialData IS NULL OR a.canAccessFinancialData = :canAccessFinancialData) AND " +
            "(:isSuperAdmin IS NULL OR a.isSuperAdmin = :isSuperAdmin) AND " +
            "a.enabled = true")
    Page<Admin> findAdminsWithFilters(@Param("department") String department,
                                      @Param("adminLevel") Admin.AdminLevel adminLevel,
                                      @Param("jobTitle") String jobTitle,
                                      @Param("minExperience") Integer minExperience,
                                      @Param("canManageUsers") Boolean canManageUsers,
                                      @Param("canAccessFinancialData") Boolean canAccessFinancialData,
                                      @Param("isSuperAdmin") Boolean isSuperAdmin,
                                      Pageable pageable);

    @Query("SELECT a FROM Admin a WHERE " +
            "LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(a.employeeId) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(a.jobTitle) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(a.department) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Admin> findByNameOrEmployeeIdOrJobTitleOrDepartmentContaining(@Param("name") String name);

    // Managed departments queries
    @Query("SELECT a FROM Admin a WHERE SIZE(a.managedDepartments) > 0")
    List<Admin> findAdminsWithManagedDepartments();

    @Query("SELECT a FROM Admin a WHERE SIZE(a.managedDepartments) >= :minCount")
    List<Admin> findAdminsWithMinManagedDepartments(@Param("minCount") int minCount);

    // Update queries
    @Modifying
    @Query("UPDATE Admin a SET a.canManageUsers = :canManage WHERE a.id = :adminId")
    void updateUserManagementPermission(@Param("adminId") Long adminId, @Param("canManage") boolean canManage);

    @Modifying
    @Query("UPDATE Admin a SET a.canAccessFinancialData = :canAccess WHERE a.id = :adminId")
    void updateFinancialDataAccessPermission(@Param("adminId") Long adminId, @Param("canAccess") boolean canAccess);

    @Modifying
    @Query("UPDATE Admin a SET a.canGenerateReports = :canGenerate WHERE a.id = :adminId")
    void updateReportGenerationPermission(@Param("adminId") Long adminId, @Param("canGenerate") boolean canGenerate);

    @Modifying
    @Query("UPDATE Admin a SET a.canModifySystemSettings = :canModify WHERE a.id = :adminId")
    void updateSystemSettingsPermission(@Param("adminId") Long adminId, @Param("canModify") boolean canModify);

    @Modifying
    @Query("UPDATE Admin a SET a.isSuperAdmin = :isSuperAdmin WHERE a.id = :adminId")
    void updateSuperAdminStatus(@Param("adminId") Long adminId, @Param("isSuperAdmin") boolean isSuperAdmin);

    @Modifying
    @Query("UPDATE Admin a SET a.adminLevel = :adminLevel WHERE a.id = :adminId")
    void updateAdminLevel(@Param("adminId") Long adminId, @Param("adminLevel") Admin.AdminLevel adminLevel);

    @Modifying
    @Query("UPDATE Admin a SET a.officeLocation = :officeLocation WHERE a.id = :adminId")
    void updateOfficeLocation(@Param("adminId") Long adminId, @Param("officeLocation") String officeLocation);

    @Modifying
    @Query("UPDATE Admin a SET a.extensionNumber = :extensionNumber WHERE a.id = :adminId")
    void updateExtensionNumber(@Param("adminId") Long adminId, @Param("extensionNumber") String extensionNumber);

    // Count queries
    long countByDepartment(String department);

    long countByAdminLevel(Admin.AdminLevel adminLevel);

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.canManageUsers = true")
    long countAdminsWhoCanManageUsers();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.canAccessFinancialData = true")
    long countAdminsWhoCanAccessFinancialData();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.canGenerateReports = true")
    long countAdminsWhoCanGenerateReports();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.canModifySystemSettings = true")
    long countAdminsWhoCanModifySystemSettings();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.isSuperAdmin = true")
    long countSuperAdmins();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.enabled = true AND a.accountLocked = false")
    long countActiveAdmins();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.officeLocation IS NOT NULL")
    long countAdminsWithAssignedOffices();

    // Statistical queries
    @Query("SELECT a.department, COUNT(a) FROM Admin a WHERE a.department IS NOT NULL GROUP BY a.department ORDER BY COUNT(a) DESC")
    List<Object[]> getAdminCountByDepartment();

    @Query("SELECT a.adminLevel, COUNT(a) FROM Admin a WHERE a.adminLevel IS NOT NULL GROUP BY a.adminLevel ORDER BY COUNT(a) DESC")
    List<Object[]> getAdminCountByLevel();

    @Query("SELECT a.jobTitle, COUNT(a) FROM Admin a WHERE a.jobTitle IS NOT NULL GROUP BY a.jobTitle ORDER BY COUNT(a) DESC")
    List<Object[]> getAdminCountByJobTitle();

    @Query("SELECT a.officeLocation, COUNT(a) FROM Admin a WHERE a.officeLocation IS NOT NULL GROUP BY a.officeLocation ORDER BY COUNT(a) DESC")
    List<Object[]> getAdminCountByOfficeLocation();

    @Query("SELECT AVG(a.yearsOfExperience) FROM Admin a WHERE a.yearsOfExperience IS NOT NULL")
    Double getAverageExperience();

    // Managed departments statistics
    @Query("SELECT md, COUNT(a) FROM Admin a JOIN a.managedDepartments md GROUP BY md ORDER BY COUNT(a) DESC")
    List<Object[]> getAdminCountByManagedDepartment();

    // Hierarchy and reporting queries
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.reportsTo = :supervisor")
    long countAdminsReportingTo(@Param("supervisor") String supervisor);

    @Query("SELECT a.reportsTo, COUNT(a) FROM Admin a WHERE a.reportsTo IS NOT NULL GROUP BY a.reportsTo ORDER BY COUNT(a) DESC")
    List<Object[]> getAdminCountBySupervisor();

    // Permission combination queries
    @Query("SELECT " +
            "SUM(CASE WHEN a.canManageUsers = true THEN 1 ELSE 0 END) as canManageUsers, " +
            "SUM(CASE WHEN a.canAccessFinancialData = true THEN 1 ELSE 0 END) as canAccessFinancialData, " +
            "SUM(CASE WHEN a.canGenerateReports = true THEN 1 ELSE 0 END) as canGenerateReports, " +
            "SUM(CASE WHEN a.canModifySystemSettings = true THEN 1 ELSE 0 END) as canModifySystemSettings, " +
            "SUM(CASE WHEN a.isSuperAdmin = true THEN 1 ELSE 0 END) as isSuperAdmin " +
            "FROM Admin a WHERE a.enabled = true")
    List<Object[]> getPermissionStatistics();
}
