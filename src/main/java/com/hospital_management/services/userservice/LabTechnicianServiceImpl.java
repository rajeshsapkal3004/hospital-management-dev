package com.hospital_management.services.userservice;

import com.hospital_management.dtos.LabTechnicianDto;
import com.hospital_management.dtos.LabTechnicianRegistrationDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.mapper.LabTechnicianMapper;
import com.hospital_management.models.LabTechnician;
import com.hospital_management.models.Role;
import com.hospital_management.repo.LabTechnicianRepository;
import com.hospital_management.repo.RoleRepository;
import com.hospital_management.services.auditlogs.AuditService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class LabTechnicianServiceImpl implements LabTechnicianService {

    private final LabTechnicianRepository labTechnicianRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LabTechnicianMapper labTechnicianMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public LabTechnicianDto registerLabTechnician(@Valid LabTechnicianRegistrationDto registrationDto) {
        log.info("Registering new lab technician with employee ID: {}", registrationDto.getEmployeeId());

        validateLabTechnicianRegistration(registrationDto);

        try {
            LabTechnician labTechnician = labTechnicianMapper.toEntity(registrationDto);
            labTechnician.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Assign LAB_TECHNICIAN role
            Role technicianRole = roleRepository.findByName(Role.RoleName.LAB_TECHNICIAN)
                    .orElseThrow(() -> new ValidationException("LAB_TECHNICIAN role not found"));
            labTechnician.getRoles().add(technicianRole);

            LabTechnician savedTechnician = labTechnicianRepository.save(labTechnician);

            auditService.logUserAction("LAB_TECHNICIAN_REGISTRATION", savedTechnician.getId(),
                    "Lab technician registered with employee ID: " + registrationDto.getEmployeeId());

            log.info("Lab technician registered successfully with ID: {}", savedTechnician.getId());
            return labTechnicianMapper.toDto(savedTechnician);

        } catch (Exception e) {
            log.error("Error registering lab technician: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register lab technician: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technicians", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('LAB_TECHNICIAN')")
    public LabTechnicianDto getLabTechnicianById(Long id) {
        log.debug("Fetching lab technician by ID: {}", id);
        LabTechnician labTechnician = labTechnicianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + id));
        return labTechnicianMapper.toDto(labTechnician);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technicians", key = "#employeeId")
    public LabTechnicianDto getLabTechnicianByEmployeeId(String employeeId) {
        log.debug("Fetching lab technician by employee ID: {}", employeeId);
        LabTechnician labTechnician = labTechnicianRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with employee ID: " + employeeId));
        return labTechnicianMapper.toDto(labTechnician);
    }

    @Override
    @Transactional(readOnly = true)
    public LabTechnicianDto getLabTechnicianByCertificationNumber(String certificationNumber) {
        log.debug("Fetching lab technician by certification number: {}", certificationNumber);
        LabTechnician labTechnician = labTechnicianRepository.findByCertificationNumber(certificationNumber)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with certification number: " + certificationNumber));
        return labTechnicianMapper.toDto(labTechnician);
    }

    @Override
    @Transactional
    @CacheEvict(value = "lab-technicians", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or (#id == authentication.principal.id and hasRole('LAB_TECHNICIAN'))")
    public LabTechnicianDto updateLabTechnician(Long id, @Valid LabTechnicianRegistrationDto updateDto) {
        log.info("Updating lab technician with ID: {}", id);

        LabTechnician existingTechnician = labTechnicianRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + id));

        validateLabTechnicianUpdate(updateDto, id);

        labTechnicianMapper.updateLabTechnicianFromDto(updateDto, existingTechnician);
        LabTechnician savedTechnician = labTechnicianRepository.save(existingTechnician);

        auditService.logUserAction("LAB_TECHNICIAN_UPDATED", id, "Lab technician profile updated");
        log.info("Lab technician updated successfully: {}", id);

        return labTechnicianMapper.toDto(savedTechnician);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateResultApprovalPermission(Long technicianId, boolean canApprove) {
        log.info("Updating result approval permission for technician {}: {}", technicianId, canApprove);

        labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        labTechnicianRepository.updateResultApprovalPermission(technicianId, canApprove);

        auditService.logUserAction("LAB_TECHNICIAN_APPROVAL_PERMISSION_UPDATED", technicianId,
                "Result approval permission updated to: " + canApprove);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateEquipmentOperationPermission(Long technicianId, boolean canOperate) {
        log.info("Updating equipment operation permission for technician {}: {}", technicianId, canOperate);

        labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        labTechnicianRepository.updateEquipmentOperationPermission(technicianId, canOperate);

        auditService.logUserAction("LAB_TECHNICIAN_EQUIPMENT_PERMISSION_UPDATED", technicianId,
                "Equipment operation permission updated to: " + canOperate);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateSeniorTechnicianStatus(Long technicianId, boolean isSenior) {
        log.info("Updating senior technician status for technician {}: {}", technicianId, isSenior);

        labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        labTechnicianRepository.updateSeniorTechnicianStatus(technicianId, isSenior);

        auditService.logUserAction("LAB_TECHNICIAN_SENIOR_STATUS_UPDATED", technicianId,
                "Senior technician status updated to: " + isSenior);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateShift(Long technicianId, LabTechnician.Shift shift) {
        log.info("Updating shift for technician {}: {}", technicianId, shift);

        labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        labTechnicianRepository.updateShift(technicianId, shift);

        auditService.logUserAction("LAB_TECHNICIAN_SHIFT_UPDATED", technicianId,
                "Shift updated to: " + shift);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateLabSection(Long technicianId, String section) {
        log.info("Updating lab section for technician {}: {}", technicianId, section);

        labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        labTechnicianRepository.updateLabSection(technicianId, section);

        auditService.logUserAction("LAB_TECHNICIAN_SECTION_UPDATED", technicianId,
                "Lab section updated to: " + section);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#technicianId == authentication.principal.id and hasRole('LAB_TECHNICIAN'))")
    public void updateCertificationExpiry(Long technicianId, LocalDate expiryDate) {
        log.info("Updating certification expiry for technician {}: {}", technicianId, expiryDate);

        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Certification expiry date cannot be in the past");
        }

        LabTechnician technician = labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        technician.setCertificationExpiry(expiryDate);
        labTechnicianRepository.save(technician);

        auditService.logUserAction("LAB_TECHNICIAN_CERTIFICATION_UPDATED", technicianId,
                "Certification expiry updated to: " + expiryDate);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('LAB_TECHNICIAN')")
    public Page<LabTechnicianDto> getAllLabTechnicians(Pageable pageable) {
        log.debug("Fetching all lab technicians with pagination");
        return labTechnicianRepository.findAll(pageable).map(labTechnicianMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "active-lab-technicians")
    public List<LabTechnicianDto> getAllActiveTechnicians() {
        log.debug("Fetching active lab technicians");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findAllActiveTechnicians());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technicians-by-department", key = "#department")
    public List<LabTechnicianDto> getTechniciansByDepartment(String department) {
        log.debug("Fetching lab technicians by department: {}", department);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findByDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technicians-by-section", key = "#labSection")
    public List<LabTechnicianDto> getTechniciansByLabSection(String labSection) {
        log.debug("Fetching lab technicians by section: {}", labSection);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findByLabSection(labSection));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technicians-by-shift", key = "#shift")
    public List<LabTechnicianDto> getTechniciansByShift(LabTechnician.Shift shift) {
        log.debug("Fetching lab technicians by shift: {}", shift);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findByShift(shift));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "approval-authorized-technicians")
    public List<LabTechnicianDto> getTechniciansWhoCanApproveResults() {
        log.debug("Fetching technicians who can approve results");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWhoCanApproveResults());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansWhoCanOperateEquipment() {
        log.debug("Fetching technicians who can operate equipment");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWhoCanOperateEquipment());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "senior-lab-technicians")
    public List<LabTechnicianDto> getSeniorTechnicians() {
        log.debug("Fetching senior technicians");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findSeniorTechnicians());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<LabTechnicianDto> getTechniciansWithExpiringCertification(LocalDate date) {
        log.debug("Fetching technicians with certification expiring before: {}", date);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithExpiringCertification(date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansWithValidCertification() {
        log.debug("Fetching technicians with valid certification");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithValidCertification());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<LabTechnicianDto> getTechniciansWithExpiredCertification() {
        log.debug("Fetching technicians with expired certification");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithExpiredCertification());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('LAB_TECHNICIAN')")
    public Page<LabTechnicianDto> searchTechnicians(String department, String labSection, LabTechnician.Shift shift,
                                                    Integer minExperience, Boolean canApproveResults,
                                                    Boolean isSeniorTechnician, Pageable pageable) {
        log.debug("Searching lab technicians with filters");
        return labTechnicianRepository.findTechniciansWithFilters(department, labSection, shift,
                        minExperience, canApproveResults, isSeniorTechnician, pageable)
                .map(labTechnicianMapper::toDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#technicianId == authentication.principal.id and hasRole('LAB_TECHNICIAN'))")
    public void addSpecialization(Long technicianId, String specialization) {
        log.info("Adding specialization to technician {}: {}", technicianId, specialization);

        LabTechnician technician = labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        technician.addSpecialization(specialization);
        labTechnicianRepository.save(technician);

        auditService.logUserAction("LAB_TECHNICIAN_SPECIALIZATION_ADDED", technicianId,
                "Specialization added: " + specialization);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#technicianId == authentication.principal.id and hasRole('LAB_TECHNICIAN'))")
    public void removeSpecialization(Long technicianId, String specialization) {
        log.info("Removing specialization from technician {}: {}", technicianId, specialization);

        LabTechnician technician = labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        technician.removeSpecialization(specialization);
        labTechnicianRepository.save(technician);

        auditService.logUserAction("LAB_TECHNICIAN_SPECIALIZATION_REMOVED", technicianId,
                "Specialization removed: " + specialization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansBySpecialization(String specialization) {
        log.debug("Fetching technicians by specialization: {}", specialization);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findBySpecialization(specialization));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansWithSpecializations() {
        log.debug("Fetching technicians with specializations");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithSpecializations());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansWithMinSpecializations(int minCount) {
        log.debug("Fetching technicians with minimum {} specializations", minCount);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithMinSpecializations(minCount));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#technicianId == authentication.principal.id and hasRole('LAB_TECHNICIAN'))")
    public void addEquipmentCertification(Long technicianId, String equipment) {
        log.info("Adding equipment certification to technician {}: {}", technicianId, equipment);

        LabTechnician technician = labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        technician.addEquipmentCertification(equipment);
        labTechnicianRepository.save(technician);

        auditService.logUserAction("LAB_TECHNICIAN_EQUIPMENT_CERTIFICATION_ADDED", technicianId,
                "Equipment certification added: " + equipment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#technicianId == authentication.principal.id and hasRole('LAB_TECHNICIAN'))")
    public void removeEquipmentCertification(Long technicianId, String equipment) {
        log.info("Removing equipment certification from technician {}: {}", technicianId, equipment);

        LabTechnician technician = labTechnicianRepository.findById(technicianId)
                .orElseThrow(() -> new UserNotFoundException("Lab technician not found with ID: " + technicianId));

        technician.removeEquipmentCertification(equipment);
        labTechnicianRepository.save(technician);

        auditService.logUserAction("LAB_TECHNICIAN_EQUIPMENT_CERTIFICATION_REMOVED", technicianId,
                "Equipment certification removed: " + equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansByCertifiedEquipment(String equipment) {
        log.debug("Fetching technicians by certified equipment: {}", equipment);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findByCertifiedEquipment(equipment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansWithCertifiedEquipment() {
        log.debug("Fetching technicians with certified equipment");
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithCertifiedEquipment());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getTechniciansWithMinEquipmentCertifications(int minCount) {
        log.debug("Fetching technicians with minimum {} equipment certifications", minCount);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findTechniciansWithMinEquipmentCertifications(minCount));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getApprovalAuthorizedTechniciansByDepartment(String department) {
        log.debug("Fetching approval authorized technicians by department: {}", department);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findApprovalAuthorizedTechniciansByDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTechnicianDto> getSeniorTechniciansBySection(String section) {
        log.debug("Fetching senior technicians by section: {}", section);
        return labTechnicianMapper.toDtoList(labTechnicianRepository.findSeniorTechniciansBySection(section));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getTechnicianCoverageByDepartmentAndShift() {
        List<Object[]> results = labTechnicianRepository.getTechnicianCoverageByDepartmentAndShift();
        Map<String, Long> coverage = new HashMap<>();
        for (Object[] result : results) {
            String department = (String) result[0];
            LabTechnician.Shift shift = (LabTechnician.Shift) result[1];
            Long count = (Long) result[2];
            coverage.put(department + "_" + shift.name(), count);
        }
        return coverage;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return labTechnicianRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCertificationNumber(String certificationNumber) {
        return labTechnicianRepository.existsByCertificationNumber(certificationNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeIdAvailable(String employeeId, Long excludeTechnicianId) {
        if (excludeTechnicianId != null) {
            return !labTechnicianRepository.existsByEmployeeIdAndIdNot(employeeId, excludeTechnicianId);
        }
        return !labTechnicianRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCertificationNumberAvailable(String certificationNumber, Long excludeTechnicianId) {
        if (excludeTechnicianId != null) {
            return !labTechnicianRepository.existsByCertificationNumberAndIdNot(certificationNumber, excludeTechnicianId);
        }
        return !labTechnicianRepository.existsByCertificationNumber(certificationNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'total-count'")
    public long getTotalTechnicianCount() {
        return labTechnicianRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'active-count'")
    public long getActiveTechnicianCount() {
        return labTechnicianRepository.countActiveTechnicians();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'senior-count'")
    public long getSeniorTechnicianCount() {
        return labTechnicianRepository.countSeniorTechnicians();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'approval-count'")
    public long getTechniciansWhoCanApproveResultsCount() {
        return labTechnicianRepository.countTechniciansWhoCanApproveResults();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'department-counts'")
    public Map<String, Long> getTechnicianCountByDepartment() {
        List<Object[]> results = labTechnicianRepository.getTechnicianCountByDepartment();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'section-counts'")
    public Map<String, Long> getTechnicianCountBySection() {
        List<Object[]> results = labTechnicianRepository.getTechnicianCountBySection();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'shift-counts'")
    public Map<String, Long> getTechnicianCountByShift() {
        List<Object[]> results = labTechnicianRepository.getTechnicianCountByShift();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            LabTechnician.Shift shift = (LabTechnician.Shift) result[0];
            counts.put(shift.name(), (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'specialization-counts'")
    public Map<String, Long> getTechnicianCountBySpecialization() {
        List<Object[]> results = labTechnicianRepository.getTechnicianCountBySpecialization();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'equipment-counts'")
    public Map<String, Long> getTechnicianCountByCertifiedEquipment() {
        List<Object[]> results = labTechnicianRepository.getTechnicianCountByCertifiedEquipment();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lab-technician-statistics", key = "'average-experience'")
    public Double getAverageExperience() {
        return labTechnicianRepository.getAverageExperience();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTechniciansWithExpiringCertificationCount(LocalDate date) {
        return labTechnicianRepository.countTechniciansWithExpiringCertification(date);
    }

    private void validateLabTechnicianRegistration(LabTechnicianRegistrationDto dto) {
        if (labTechnicianRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }

        if (dto.getCertificationNumber() != null &&
                labTechnicianRepository.existsByCertificationNumber(dto.getCertificationNumber())) {
            throw new UserAlreadyExistsException("certification number", dto.getCertificationNumber());
        }
    }

    private void validateLabTechnicianUpdate(LabTechnicianRegistrationDto dto, Long technicianId) {
        if (!isEmployeeIdAvailable(dto.getEmployeeId(), technicianId)) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }

        if (dto.getCertificationNumber() != null &&
                !isCertificationNumberAvailable(dto.getCertificationNumber(), technicianId)) {
            throw new UserAlreadyExistsException("certification number", dto.getCertificationNumber());
        }
    }
}

