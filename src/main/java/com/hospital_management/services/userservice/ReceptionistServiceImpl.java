package com.hospital_management.services.userservice;



import com.hospital_management.dtos.ReceptionistDto;
import com.hospital_management.dtos.ReceptionistRegistrationDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.mapper.ReceptionistMapper;
import com.hospital_management.models.Receptionist;
import com.hospital_management.models.Role;
import com.hospital_management.repo.ReceptionistRepository;
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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class ReceptionistServiceImpl implements ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReceptionistMapper receptionistMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ReceptionistDto registerReceptionist(@Valid ReceptionistRegistrationDto registrationDto) {
        log.info("Registering new receptionist with employee ID: {}", registrationDto.getEmployeeId());

        validateReceptionistRegistration(registrationDto);

        try {
            Receptionist receptionist = receptionistMapper.toEntity(registrationDto);
            receptionist.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Assign RECEPTIONIST role
            Role receptionistRole = roleRepository.findByName(Role.RoleName.RECEPTIONIST)
                    .orElseThrow(() -> new ValidationException("RECEPTIONIST role not found"));
            receptionist.getRoles().add(receptionistRole);

            Receptionist savedReceptionist = receptionistRepository.save(receptionist);

            auditService.logUserAction("RECEPTIONIST_REGISTRATION", savedReceptionist.getId(),
                    "Receptionist registered with employee ID: " + registrationDto.getEmployeeId());

            log.info("Receptionist registered successfully with ID: {}", savedReceptionist.getId());
            return receptionistMapper.toDto(savedReceptionist);

        } catch (Exception e) {
            log.error("Error registering receptionist: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register receptionist: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionists", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public ReceptionistDto getReceptionistById(Long id) {
        log.debug("Fetching receptionist by ID: {}", id);
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + id));
        return receptionistMapper.toDto(receptionist);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionists", key = "#employeeId")
    public ReceptionistDto getReceptionistByEmployeeId(String employeeId) {
        log.debug("Fetching receptionist by employee ID: {}", employeeId);
        Receptionist receptionist = receptionistRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with employee ID: " + employeeId));
        return receptionistMapper.toDto(receptionist);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionistDto getReceptionistByExtensionNumber(String extensionNumber) {
        log.debug("Fetching receptionist by extension number: {}", extensionNumber);
        Receptionist receptionist = receptionistRepository.findByExtensionNumber(extensionNumber)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with extension number: " + extensionNumber));
        return receptionistMapper.toDto(receptionist);
    }

    @Override
    @Transactional
    @CacheEvict(value = "receptionists", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or (#id == authentication.principal.id and hasRole('RECEPTIONIST'))")
    public ReceptionistDto updateReceptionist(Long id, @Valid ReceptionistRegistrationDto updateDto) {
        log.info("Updating receptionist with ID: {}", id);

        Receptionist existingReceptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + id));

        validateReceptionistUpdate(updateDto, id);

        receptionistMapper.updateReceptionistFromDto(updateDto, existingReceptionist);
        Receptionist savedReceptionist = receptionistRepository.save(existingReceptionist);

        auditService.logUserAction("RECEPTIONIST_UPDATED", id, "Receptionist profile updated");
        log.info("Receptionist updated successfully: {}", id);

        return receptionistMapper.toDto(savedReceptionist);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateShift(Long receptionistId, Receptionist.Shift shift) {
        log.info("Updating shift for receptionist {}: {}", receptionistId, shift);

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updateShift(receptionistId, shift);

        auditService.logUserAction("RECEPTIONIST_SHIFT_UPDATED", receptionistId,
                "Shift updated to: " + shift);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateShiftTimes(Long receptionistId, LocalTime startTime, LocalTime endTime) {
        log.info("Updating shift times for receptionist {}: {} to {}", receptionistId, startTime, endTime);

        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Shift start time cannot be after end time");
        }

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updateShiftTimes(receptionistId, startTime, endTime);

        auditService.logUserAction("RECEPTIONIST_SHIFT_TIMES_UPDATED", receptionistId,
                String.format("Shift times updated: %s to %s", startTime, endTime));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateDeskLocation(Long receptionistId, String deskLocation) {
        log.info("Updating desk location for receptionist {}: {}", receptionistId, deskLocation);

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updateDeskLocation(receptionistId, deskLocation);

        auditService.logUserAction("RECEPTIONIST_DESK_UPDATED", receptionistId,
                "Desk location updated to: " + deskLocation);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateExtensionNumber(Long receptionistId, String extensionNumber) {
        log.info("Updating extension number for receptionist {}: {}", receptionistId, extensionNumber);

        if (extensionNumber != null && receptionistRepository.existsByExtensionNumberAndIdNot(extensionNumber, receptionistId)) {
            throw new UserAlreadyExistsException("extension number", extensionNumber);
        }

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updateExtensionNumber(receptionistId, extensionNumber);

        auditService.logUserAction("RECEPTIONIST_EXTENSION_UPDATED", receptionistId,
                "Extension number updated to: " + extensionNumber);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updatePaymentHandlingPermission(Long receptionistId, boolean canHandle) {
        log.info("Updating payment handling permission for receptionist {}: {}", receptionistId, canHandle);

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updatePaymentHandlingPermission(receptionistId, canHandle);

        auditService.logUserAction("RECEPTIONIST_PAYMENT_PERMISSION_UPDATED", receptionistId,
                "Payment handling permission updated to: " + canHandle);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateAppointmentSchedulingPermission(Long receptionistId, boolean canSchedule) {
        log.info("Updating appointment scheduling permission for receptionist {}: {}", receptionistId, canSchedule);

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updateAppointmentSchedulingPermission(receptionistId, canSchedule);

        auditService.logUserAction("RECEPTIONIST_APPOINTMENT_PERMISSION_UPDATED", receptionistId,
                "Appointment scheduling permission updated to: " + canSchedule);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateMedicalRecordAccessPermission(Long receptionistId, boolean canAccess) {
        log.info("Updating medical record access permission for receptionist {}: {}", receptionistId, canAccess);

        receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new UserNotFoundException("Receptionist not found with ID: " + receptionistId));

        receptionistRepository.updateMedicalRecordAccessPermission(receptionistId, canAccess);

        auditService.logUserAction("RECEPTIONIST_MEDICAL_RECORD_PERMISSION_UPDATED", receptionistId,
                "Medical record access permission updated to: " + canAccess);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public Page<ReceptionistDto> getAllReceptionists(Pageable pageable) {
        log.debug("Fetching all receptionists with pagination");
        return receptionistRepository.findAll(pageable).map(receptionistMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "active-receptionists")
    public List<ReceptionistDto> getAllActiveReceptionists() {
        log.debug("Fetching active receptionists");
        return receptionistMapper.toDtoList(receptionistRepository.findAllActiveReceptionists());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionists-by-department", key = "#department")
    public List<ReceptionistDto> getReceptionistsByDepartment(String department) {
        log.debug("Fetching receptionists by department: {}", department);
        return receptionistMapper.toDtoList(receptionistRepository.findByDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionists-by-shift", key = "#shift")
    public List<ReceptionistDto> getReceptionistsByShift(Receptionist.Shift shift) {
        log.debug("Fetching receptionists by shift: {}", shift);
        return receptionistMapper.toDtoList(receptionistRepository.findByShift(shift));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistDto> getReceptionistsByDeskLocation(String deskLocation) {
        log.debug("Fetching receptionists by desk location: {}", deskLocation);
        return receptionistMapper.toDtoList(receptionistRepository.findByDeskLocation(deskLocation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistDto> getReceptionistsCurrentlyOnDuty(LocalTime currentTime) {
        log.debug("Fetching receptionists currently on duty at: {}", currentTime);
        return receptionistMapper.toDtoList(receptionistRepository.findReceptionistsCurrentlyOnDuty(currentTime));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistDto> getReceptionistsWhoCanHandlePayments() {
        log.debug("Fetching receptionists who can handle payments");
        return receptionistMapper.toDtoList(receptionistRepository.findReceptionistsWhoCanHandlePayments());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistDto> getReceptionistsWhoCanScheduleAppointments() {
        log.debug("Fetching receptionists who can schedule appointments");
        return receptionistMapper.toDtoList(receptionistRepository.findReceptionistsWhoCanScheduleAppointments());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistDto> getReceptionistsWhoCanAccessMedicalRecords() {
        log.debug("Fetching receptionists who can access medical records");
        return receptionistMapper.toDtoList(receptionistRepository.findReceptionistsWhoCanAccessMedicalRecords());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionistDto> getFullServiceReceptionists() {
        log.debug("Fetching full service receptionists");
        return receptionistMapper.toDtoList(receptionistRepository.findFullServiceReceptionists());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public Page<ReceptionistDto> searchReceptionists(String department, Receptionist.Shift shift,
                                                     String deskLocation, Integer minExperience,
                                                     Boolean canHandlePayments, Boolean canScheduleAppointments,
                                                     Pageable pageable) {
        log.debug("Searching receptionists with filters");
        return receptionistRepository.findReceptionistsWithFilters(department, shift, deskLocation,
                        minExperience, canHandlePayments, canScheduleAppointments, pageable)
                .map(receptionistMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return receptionistRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByExtensionNumber(String extensionNumber) {
        return receptionistRepository.existsByExtensionNumber(extensionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeIdAvailable(String employeeId, Long excludeReceptionistId) {
        if (excludeReceptionistId != null) {
            return !receptionistRepository.existsByEmployeeIdAndIdNot(employeeId, excludeReceptionistId);
        }
        return !receptionistRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExtensionNumberAvailable(String extensionNumber, Long excludeReceptionistId) {
        if (excludeReceptionistId != null) {
            return !receptionistRepository.existsByExtensionNumberAndIdNot(extensionNumber, excludeReceptionistId);
        }
        return !receptionistRepository.existsByExtensionNumber(extensionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionist-statistics", key = "'total-count'")
    public long getTotalReceptionistCount() {
        return receptionistRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionist-statistics", key = "'active-count'")
    public long getActiveReceptionistCount() {
        return receptionistRepository.countActiveReceptionists();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionist-statistics", key = "'department-counts'")
    public Map<String, Long> getReceptionistCountByDepartment() {
        List<Object[]> results = receptionistRepository.getReceptionistCountByDepartment();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionist-statistics", key = "'shift-counts'")
    public Map<String, Long> getReceptionistCountByShift() {
        List<Object[]> results = receptionistRepository.getReceptionistCountByShift();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            Receptionist.Shift shift = (Receptionist.Shift) result[0];
            counts.put(shift.name(), (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionist-statistics", key = "'desk-counts'")
    public Map<String, Long> getReceptionistCountByDeskLocation() {
        List<Object[]> results = receptionistRepository.getReceptionistCountByDeskLocation();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "receptionist-statistics", key = "'average-experience'")
    public Double getAverageExperience() {
        return receptionistRepository.getAverageExperience();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getReceptionistCoverageByDepartmentAndShift() {
        List<Object[]> results = receptionistRepository.getReceptionistCoverageByDepartmentAndShift();
        Map<String, Long> coverage = new HashMap<>();
        for (Object[] result : results) {
            String department = (String) result[0];
            Receptionist.Shift shift = (Receptionist.Shift) result[1];
            Long count = (Long) result[2];
            coverage.put(department + "_" + shift.name(), count);
        }
        return coverage;
    }

    private void validateReceptionistRegistration(ReceptionistRegistrationDto dto) {
        if (receptionistRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }

        if (dto.getExtensionNumber() != null &&
                receptionistRepository.existsByExtensionNumber(dto.getExtensionNumber())) {
            throw new UserAlreadyExistsException("extension number", dto.getExtensionNumber());
        }

        if (dto.getShiftStartTime().isAfter(dto.getShiftEndTime())) {
            throw new ValidationException("Shift start time cannot be after end time");
        }
    }

    private void validateReceptionistUpdate(ReceptionistRegistrationDto dto, Long receptionistId) {
        if (!isEmployeeIdAvailable(dto.getEmployeeId(), receptionistId)) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }

        if (dto.getExtensionNumber() != null &&
                !isExtensionNumberAvailable(dto.getExtensionNumber(), receptionistId)) {
            throw new UserAlreadyExistsException("extension number", dto.getExtensionNumber());
        }

        if (dto.getShiftStartTime().isAfter(dto.getShiftEndTime())) {
            throw new ValidationException("Shift start time cannot be after end time");
        }
    }
}

