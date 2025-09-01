package com.hospital_management.services.userservice;

import com.hospital_management.dtos.NurseDto;
import com.hospital_management.dtos.NurseRegistrationDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.mapper.NurseMapper;
import com.hospital_management.models.Nurse;
import com.hospital_management.models.Role;
import com.hospital_management.repo.NurseRepository;
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
public class NurseServiceImpl implements NurseService {

    private final NurseRepository nurseRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final NurseMapper nurseMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public NurseDto registerNurse(@Valid NurseRegistrationDto registrationDto) {
        log.info("Registering new nurse with license: {}", registrationDto.getLicenseNumber());

        validateNurseRegistration(registrationDto);

        try {
            Nurse nurse = nurseMapper.toEntity(registrationDto);
            nurse.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Assign NURSE role
            Role nurseRole = roleRepository.findByName(Role.RoleName.NURSE)
                    .orElseThrow(() -> new ValidationException("NURSE role not found"));
            nurse.getRoles().add(nurseRole);

            Nurse savedNurse = nurseRepository.save(nurse);

            auditService.logUserAction("NURSE_REGISTRATION", savedNurse.getId(),
                    "Nurse registered with license: " + registrationDto.getLicenseNumber());

            log.info("Nurse registered successfully with ID: {}", savedNurse.getId());
            return nurseMapper.toDto(savedNurse);

        } catch (Exception e) {
            log.error("Error registering nurse: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register nurse: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurses", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public NurseDto getNurseById(Long id) {
        log.debug("Fetching nurse by ID: {}", id);
        Nurse nurse = nurseRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + id));
        return nurseMapper.toDto(nurse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurses", key = "#licenseNumber")
    public NurseDto getNurseByLicenseNumber(String licenseNumber) {
        log.debug("Fetching nurse by license number: {}", licenseNumber);
        Nurse nurse = nurseRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with license number: " + licenseNumber));
        return nurseMapper.toDto(nurse);
    }

    @Override
    @Transactional(readOnly = true)
    public NurseDto getNurseByEmployeeId(String employeeId) {
        log.debug("Fetching nurse by employee ID: {}", employeeId);
        Nurse nurse = nurseRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with employee ID: " + employeeId));
        return nurseMapper.toDto(nurse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "nurses", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or (#id == authentication.principal.id and hasRole('NURSE'))")
    public NurseDto updateNurse(Long id, @Valid NurseRegistrationDto updateDto) {
        log.info("Updating nurse with ID: {}", id);

        Nurse existingNurse = nurseRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + id));

        validateNurseUpdate(updateDto, id);

        nurseMapper.updateNurseFromDto(updateDto, existingNurse);
        Nurse savedNurse = nurseRepository.save(existingNurse);

        auditService.logUserAction("NURSE_UPDATED", id, "Nurse profile updated");
        log.info("Nurse updated successfully: {}", id);

        return nurseMapper.toDto(savedNurse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#nurseId == authentication.principal.id and hasRole('NURSE'))")
    public void updateAvailabilityStatus(Long nurseId, boolean available) {
        log.info("Updating availability status for nurse {}: {}", nurseId, available);

        nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurseRepository.updateAvailabilityStatus(nurseId, available);

        String action = available ? "NURSE_AVAILABLE" : "NURSE_UNAVAILABLE";
        auditService.logUserAction(action, nurseId, "Availability status updated to: " + available);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateShift(Long nurseId, Nurse.Shift shift) {
        log.info("Updating shift for nurse {}: {}", nurseId, shift);

        nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurseRepository.updateShift(nurseId, shift);

        auditService.logUserAction("NURSE_SHIFT_UPDATED", nurseId, "Shift updated to: " + shift);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateWardAssignment(Long nurseId, String ward) {
        log.info("Updating ward assignment for nurse {}: {}", nurseId, ward);

        nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurseRepository.updateWardAssignment(nurseId, ward);

        auditService.logUserAction("NURSE_WARD_UPDATED", nurseId, "Ward assignment updated to: " + ward);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateHeadNurseStatus(Long nurseId, boolean isHeadNurse) {
        log.info("Updating head nurse status for nurse {}: {}", nurseId, isHeadNurse);

        nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurseRepository.updateHeadNurseStatus(nurseId, isHeadNurse);

        auditService.logUserAction("NURSE_HEAD_STATUS_UPDATED", nurseId,
                "Head nurse status updated to: " + isHeadNurse);
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateMedicationAdministrationPermission(Long nurseId, boolean canAdminister) {
        log.info("Updating medication administration permission for nurse {}: {}", nurseId, canAdminister);

        nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurseRepository.updateMedicationAdministrationPermission(nurseId, canAdminister);

        auditService.logUserAction("NURSE_MEDICATION_PERMISSION_UPDATED", nurseId,
                "Medication administration permission updated to: " + canAdminister);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public Page<NurseDto> getAllNurses(Pageable pageable) {
        log.debug("Fetching all nurses with pagination");
        return nurseRepository.findAll(pageable).map(nurseMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "available-nurses")
    public List<NurseDto> getAvailableNurses() {
        log.debug("Fetching available nurses");
        return nurseMapper.toDtoList(nurseRepository.findAvailableNurses());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurses-by-department", key = "#department")
    public List<NurseDto> getNursesByDepartment(String department) {
        log.debug("Fetching nurses by department: {}", department);
        return nurseMapper.toDtoList(nurseRepository.findByDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurses-by-shift", key = "#shift")
    public List<NurseDto> getNursesByShift(Nurse.Shift shift) {
        log.debug("Fetching nurses by shift: {}", shift);
        return nurseMapper.toDtoList(nurseRepository.findByShift(shift));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurses-by-ward", key = "#ward")
    public List<NurseDto> getNursesByWard(String ward) {
        log.debug("Fetching nurses by ward: {}", ward);
        return nurseMapper.toDtoList(nurseRepository.findByWardAssignment(ward));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "head-nurses")
    public List<NurseDto> getHeadNurses() {
        log.debug("Fetching head nurses");
        return nurseMapper.toDtoList(nurseRepository.findHeadNurses());
    }


    @Transactional(readOnly = true)
    public List<NurseDto> getNursesByType(Nurse.NurseType nurseType) {
        log.debug("Fetching nurses by type: {}", nurseType);
        return nurseMapper.toDtoList(nurseRepository.findByNurseType(nurseType));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<NurseDto> getNursesWithExpiringCertification(LocalDate date) {
        log.debug("Fetching nurses with certification expiring before: {}", date);
        return nurseMapper.toDtoList(nurseRepository.findNursesWithExpiringCertification(date));
    }


    @Transactional(readOnly = true)
    public List<NurseDto> getNursesWhoCanAdministerMedication() {
        log.debug("Fetching nurses who can administer medication");
        return nurseMapper.toDtoList(nurseRepository.findNursesWhoCanAdministerMedication());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public Page<NurseDto> searchNurses(String department, Nurse.Shift shift, Nurse.NurseType nurseType,
                                       String wardAssignment, Integer minExperience, Boolean available,
                                       Boolean headNurse, Pageable pageable) {
        log.debug("Searching nurses with filters");
        return nurseRepository.findNursesWithFilters(department, shift, nurseType, wardAssignment,
                        minExperience, available, headNurse, pageable)
                .map(nurseMapper::toDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#nurseId == authentication.principal.id and hasRole('NURSE'))")
    public void addSpecialization(Long nurseId, String specialization) {
        log.info("Adding specialization to nurse {}: {}", nurseId, specialization);

        Nurse nurse = nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurse.addSpecialization(specialization);
        nurseRepository.save(nurse);

        auditService.logUserAction("NURSE_SPECIALIZATION_ADDED", nurseId,
                "Specialization added: " + specialization);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#nurseId == authentication.principal.id and hasRole('NURSE'))")
    public void removeSpecialization(Long nurseId, String specialization) {
        log.info("Removing specialization from nurse {}: {}", nurseId, specialization);

        Nurse nurse = nurseRepository.findById(nurseId)
                .orElseThrow(() -> new UserNotFoundException("Nurse not found with ID: " + nurseId));

        nurse.removeSpecialization(specialization);
        nurseRepository.save(nurse);

        auditService.logUserAction("NURSE_SPECIALIZATION_REMOVED", nurseId,
                "Specialization removed: " + specialization);
    }


    @Transactional(readOnly = true)
    public List<NurseDto> getNursesBySpecialization(String specialization) {
        log.debug("Fetching nurses by specialization: {}", specialization);
        return nurseMapper.toDtoList(nurseRepository.findBySpecialization(specialization));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLicenseNumber(String licenseNumber) {
        return nurseRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return nurseRepository.existsByEmployeeId(employeeId);
    }


    @Transactional(readOnly = true)
    public boolean isLicenseNumberAvailable(String licenseNumber, Long excludeNurseId) {
        if (excludeNurseId != null) {
            return !nurseRepository.existsByLicenseNumberAndIdNot(licenseNumber, excludeNurseId);
        }
        return !nurseRepository.existsByLicenseNumber(licenseNumber);
    }


    @Transactional(readOnly = true)
    public boolean isEmployeeIdAvailable(String employeeId, Long excludeNurseId) {
        if (excludeNurseId != null) {
            return !nurseRepository.existsByEmployeeIdAndIdNot(employeeId, excludeNurseId);
        }
        return !nurseRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'total-count'")
    public long getTotalNurseCount() {
        return nurseRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'available-count'")
    public long getAvailableNurseCount() {
        return nurseRepository.countAvailableNurses();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'head-count'")
    public long getHeadNurseCount() {
        return nurseRepository.countHeadNurses();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'department-counts'")
    public Map<String, Long> getNurseCountByDepartment() {
        List<Object[]> results = nurseRepository.getNurseCountByDepartment();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'shift-counts'")
    public Map<String, Long> getNurseCountByShift() {
        List<Object[]> results = nurseRepository.getNurseCountByShift();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            Nurse.Shift shift = (Nurse.Shift) result[0];
            counts.put(shift.name(), (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'type-counts'")
    public Map<String, Long> getNurseCountByType() {
        List<Object[]> results = nurseRepository.getNurseCountByType();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            Nurse.NurseType type = (Nurse.NurseType) result[0];
            counts.put(type.name(), (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'ward-counts'")
    public Map<String, Long> getNurseCountByWard() {
        List<Object[]> results = nurseRepository.getNurseCountByWard();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "nurse-statistics", key = "'average-experience'")
    public Double getAverageExperience() {
        return nurseRepository.getAverageExperience();
    }

    private void validateNurseRegistration(NurseRegistrationDto dto) {
        if (nurseRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new UserAlreadyExistsException("license number", dto.getLicenseNumber());
        }

        if (nurseRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }
    }

    private void validateNurseUpdate(NurseRegistrationDto dto, Long nurseId) {
        if (!isLicenseNumberAvailable(dto.getLicenseNumber(), nurseId)) {
            throw new UserAlreadyExistsException("license number", dto.getLicenseNumber());
        }

        if (!isEmployeeIdAvailable(dto.getEmployeeId(), nurseId)) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }
    }
}
