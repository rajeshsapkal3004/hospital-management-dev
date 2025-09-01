package com.hospital_management.services.userservice;


import com.hospital_management.dtos.LabTechnicianDto;
import com.hospital_management.dtos.LabTechnicianRegistrationDto;
import com.hospital_management.models.LabTechnician;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LabTechnicianService {

    // Registration and basic operations
    LabTechnicianDto registerLabTechnician(LabTechnicianRegistrationDto registrationDto);
    LabTechnicianDto getLabTechnicianById(Long id);
    LabTechnicianDto getLabTechnicianByEmployeeId(String employeeId);
    LabTechnicianDto getLabTechnicianByCertificationNumber(String certificationNumber);

    // Update operations
    LabTechnicianDto updateLabTechnician(Long id, LabTechnicianRegistrationDto updateDto);
    void updateResultApprovalPermission(Long technicianId, boolean canApprove);
    void updateEquipmentOperationPermission(Long technicianId, boolean canOperate);
    void updateSeniorTechnicianStatus(Long technicianId, boolean isSenior);
    void updateShift(Long technicianId, LabTechnician.Shift shift);
    void updateLabSection(Long technicianId, String section);
    void updateCertificationExpiry(Long technicianId, LocalDate expiryDate);

    // Search and filtering
    Page<LabTechnicianDto> getAllLabTechnicians(Pageable pageable);
    List<LabTechnicianDto> getAllActiveTechnicians();
    List<LabTechnicianDto> getTechniciansByDepartment(String department);
    List<LabTechnicianDto> getTechniciansByLabSection(String labSection);
    List<LabTechnicianDto> getTechniciansByShift(LabTechnician.Shift shift);
    List<LabTechnicianDto> getTechniciansWhoCanApproveResults();
    List<LabTechnicianDto> getTechniciansWhoCanOperateEquipment();
    List<LabTechnicianDto> getSeniorTechnicians();
    List<LabTechnicianDto> getTechniciansWithExpiringCertification(LocalDate date);
    List<LabTechnicianDto> getTechniciansWithValidCertification();
    List<LabTechnicianDto> getTechniciansWithExpiredCertification();
    Page<LabTechnicianDto> searchTechnicians(String department, String labSection, LabTechnician.Shift shift,
                                             Integer minExperience, Boolean canApproveResults,
                                             Boolean isSeniorTechnician, Pageable pageable);

    // Specialization management
    void addSpecialization(Long technicianId, String specialization);
    void removeSpecialization(Long technicianId, String specialization);
    List<LabTechnicianDto> getTechniciansBySpecialization(String specialization);
    List<LabTechnicianDto> getTechniciansWithSpecializations();
    List<LabTechnicianDto> getTechniciansWithMinSpecializations(int minCount);

    // Equipment certification management
    void addEquipmentCertification(Long technicianId, String equipment);
    void removeEquipmentCertification(Long technicianId, String equipment);
    List<LabTechnicianDto> getTechniciansByCertifiedEquipment(String equipment);
    List<LabTechnicianDto> getTechniciansWithCertifiedEquipment();
    List<LabTechnicianDto> getTechniciansWithMinEquipmentCertifications(int minCount);

    // Department coverage
    List<LabTechnicianDto> getApprovalAuthorizedTechniciansByDepartment(String department);
    List<LabTechnicianDto> getSeniorTechniciansBySection(String section);
    Map<String, Long> getTechnicianCoverageByDepartmentAndShift();

    // Validation
    boolean existsByEmployeeId(String employeeId);
    boolean existsByCertificationNumber(String certificationNumber);
    boolean isEmployeeIdAvailable(String employeeId, Long excludeTechnicianId);
    boolean isCertificationNumberAvailable(String certificationNumber, Long excludeTechnicianId);

    // Statistics
    long getTotalTechnicianCount();
    long getActiveTechnicianCount();
    long getSeniorTechnicianCount();
    long getTechniciansWhoCanApproveResultsCount();
    Map<String, Long> getTechnicianCountByDepartment();
    Map<String, Long> getTechnicianCountBySection();
    Map<String, Long> getTechnicianCountByShift();
    Map<String, Long> getTechnicianCountBySpecialization();
    Map<String, Long> getTechnicianCountByCertifiedEquipment();
    Double getAverageExperience();
    long getTechniciansWithExpiringCertificationCount(LocalDate date);
}
