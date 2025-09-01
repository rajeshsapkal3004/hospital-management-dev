package com.hospital_management.services.userservice;



import com.hospital_management.dtos.DoctorDto;
import com.hospital_management.dtos.DoctorRegistrationDto;
import com.hospital_management.exception.UserAlreadyExistsException;
import com.hospital_management.exception.UserNotFoundException;
import com.hospital_management.mapper.DoctorMapper;
import com.hospital_management.models.Doctor;
import com.hospital_management.models.Role;
import com.hospital_management.repo.DoctorRepository;
import com.hospital_management.repo.RoleRepository;
import com.hospital_management.services.auditlogs.AuditServiceImpl;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final AuditServiceImpl auditServiceImpl;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorDto registerDoctor(@Valid DoctorRegistrationDto registrationDto) {
        log.info("Registering new doctor with license: {}", registrationDto.getLicenseNumber());

        // Validation
        validateDoctorRegistration(registrationDto);

        try {
            // Create doctor entity
            Doctor doctor = doctorMapper.toEntity(registrationDto);
            doctor.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Assign DOCTOR role
            Role doctorRole = roleRepository.findByName(Role.RoleName.DOCTOR)
                    .orElseThrow(() -> new ValidationException("DOCTOR role not found"));
            doctor.getRoles().add(doctorRole);

            Doctor savedDoctor = doctorRepository.save(doctor);

            // Audit logging
            auditServiceImpl.logUserAction("DOCTOR_REGISTRATION", savedDoctor.getId(),
                    "Doctor registered with license: " + registrationDto.getLicenseNumber());

            log.info("Doctor registered successfully with ID: {}", savedDoctor.getId());
            return doctorMapper.toDto(savedDoctor);

        } catch (Exception e) {
            log.error("Error registering doctor: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register doctor: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public DoctorDto getDoctorById(Long id) {
        log.debug("Fetching doctor by ID: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + id));
        return doctorMapper.toDto(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors", key = "#licenseNumber")
    public DoctorDto getDoctorByLicenseNumber(String licenseNumber) {
        log.debug("Fetching doctor by license number: {}", licenseNumber);
        Doctor doctor = doctorRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with license number: " + licenseNumber));
        return doctorMapper.toDto(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDto getDoctorByEmployeeId(String employeeId) {
        log.debug("Fetching doctor by employee ID: {}", employeeId);
        Doctor doctor = doctorRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with employee ID: " + employeeId));
        return doctorMapper.toDto(doctor);
    }

    @Override
    @Transactional
    @CacheEvict(value = "doctors", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or (#id == authentication.principal.id and hasRole('DOCTOR'))")
    public DoctorDto updateDoctor(Long id, @Valid DoctorRegistrationDto updateDto) {
        log.info("Updating doctor with ID: {}", id);

        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + id));

        // Validate unique constraints
        if (!existingDoctor.getLicenseNumber().equals(updateDto.getLicenseNumber()) &&
                doctorRepository.existsByLicenseNumber(updateDto.getLicenseNumber())) {
            throw new UserAlreadyExistsException("license number", updateDto.getLicenseNumber());
        }

        if (updateDto.getNpiNumber() != null &&
                !updateDto.getNpiNumber().equals(existingDoctor.getNpiNumber()) &&
                doctorRepository.existsByNpiNumber(updateDto.getNpiNumber())) {
            throw new UserAlreadyExistsException("NPI number", updateDto.getNpiNumber());
        }

        // Update doctor
        doctorMapper.updateDoctorFromDto(updateDto, existingDoctor);
        Doctor savedDoctor = doctorRepository.save(existingDoctor);

        auditServiceImpl.logUserAction("DOCTOR_UPDATED", id, "Doctor profile updated");
        log.info("Doctor updated successfully: {}", id);

        return doctorMapper.toDto(savedDoctor);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#doctorId == authentication.principal.id and hasRole('DOCTOR'))")
    public void updateAvailabilityStatus(Long doctorId, boolean available) {
        log.info("Updating availability status for doctor {}: {}", doctorId, available);

        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + doctorId));

        doctorRepository.updateAvailabilityStatus(doctorId, available);

        String action = available ? "DOCTOR_AVAILABLE" : "DOCTOR_UNAVAILABLE";
        auditServiceImpl.logUserAction(action, doctorId, "Availability status updated to: " + available);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#doctorId == authentication.principal.id and hasRole('DOCTOR'))")
    public void updateConsultationFee(Long doctorId, BigDecimal fee) {
        log.info("Updating consultation fee for doctor {}: {}", doctorId, fee);

        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Consultation fee must be positive");
        }

        if (fee.compareTo(new BigDecimal("9999.99")) > 0) {
            throw new ValidationException("Consultation fee cannot exceed 9999.99");
        }

        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + doctorId));

        doctorRepository.updateConsultationFee(doctorId, fee);

        auditServiceImpl.logUserAction("CONSULTATION_FEE_UPDATED", doctorId,
                "Consultation fee updated to: " + fee);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "available-doctors")
    public List<DoctorDto> getAvailableDoctors() {
        log.debug("Fetching available doctors");
        return doctorMapper.toDtoList(doctorRepository.findAvailableDoctors());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors-by-specialization", key = "#specialization")
    public List<DoctorDto> getDoctorsBySpecialization(String specialization) {
        log.debug("Fetching doctors by specialization: {}", specialization);
        return doctorMapper.toDtoList(doctorRepository.findBySpecialization(specialization));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctors-by-department", key = "#department")
    public List<DoctorDto> getDoctorsByDepartment(String department) {
        log.debug("Fetching doctors by department: {}", department);
        return doctorMapper.toDtoList(doctorRepository.findByDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoctorDto> getDoctorsWithExpiringLicense(LocalDate date) {
        log.debug("Fetching doctors with license expiring before: {}", date);
        return doctorMapper.toDtoList(doctorRepository.findDoctorsWithExpiringLicense(date));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public Page<DoctorDto> searchDoctors(String specialization, String department,
                                         Integer minExperience, BigDecimal maxFee,
                                         Boolean available, Pageable pageable) {
        log.debug("Searching doctors with filters");
        return doctorRepository.findDoctorsWithFilters(specialization, department,
                        minExperience, maxFee, available, pageable)
                .map(doctorMapper::toDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#doctorId == authentication.principal.id and hasRole('DOCTOR'))")
    public void addCertification(Long doctorId, String certification) {
        log.info("Adding certification to doctor {}: {}", doctorId, certification);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + doctorId));

        doctor.addCertification(certification);
        doctorRepository.save(doctor);

        auditServiceImpl.logUserAction("CERTIFICATION_ADDED", doctorId,
                "Certification added: " + certification);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLicenseNumber(String licenseNumber) {
        return doctorRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmployeeId(String employeeId) {
        return doctorRepository.existsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLicenseNumberAvailable(String licenseNumber, Long excludeDoctorId) {
        if (excludeDoctorId != null) {
            return !doctorRepository.existsByLicenseNumberAndIdNot(licenseNumber, excludeDoctorId);
        }
        return !doctorRepository.existsByLicenseNumber(licenseNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctor-statistics", key = "'total-count'")
    public long getTotalDoctorCount() {
        return doctorRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctor-statistics", key = "'available-count'")
    public long getAvailableDoctorCount() {
        return doctorRepository.countAvailableDoctors();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctor-statistics", key = "'specialization-counts'")
    public Map<String, Long> getDoctorCountBySpecialization() {
        List<Object[]> results = doctorRepository.getDoctorCountBySpecialization();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctor-statistics", key = "'department-counts'")
    public Map<String, Long> getDoctorCountByDepartment() {
        List<Object[]> results = doctorRepository.getDoctorCountByDepartment();
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put((String) result[0], (Long) result[1]);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "doctor-statistics", key = "'average-fee'")
    public BigDecimal getAverageConsultationFee() {
        return doctorRepository.getAverageConsultationFee();
    }

    // Private helper methods
    private void validateDoctorRegistration(DoctorRegistrationDto dto) {
        if (doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new UserAlreadyExistsException("license number", dto.getLicenseNumber());
        }

        if (dto.getNpiNumber() != null && doctorRepository.existsByNpiNumber(dto.getNpiNumber())) {
            throw new UserAlreadyExistsException("NPI number", dto.getNpiNumber());
        }

        if (dto.getEmployeeId() != null && doctorRepository.existsByEmployeeId(dto.getEmployeeId())) {
            throw new UserAlreadyExistsException("employee ID", dto.getEmployeeId());
        }
    }

    // Additional method implementations...
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public Page<DoctorDto> getAllDoctors(Pageable pageable) {
        log.debug("Fetching all doctors with pagination");
        return doctorRepository.findAll(pageable).map(doctorMapper::toDto);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#doctorId == authentication.principal.id and hasRole('DOCTOR'))")
    public void updateRoomNumber(Long doctorId, String roomNumber) {
        log.info("Updating room number for doctor {}: {}", doctorId, roomNumber);

        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + doctorId));

        doctorRepository.updateRoomNumber(doctorId, roomNumber);

        auditServiceImpl.logUserAction("ROOM_NUMBER_UPDATED", doctorId,
                "Room number updated to: " + roomNumber);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (#doctorId == authentication.principal.id and hasRole('DOCTOR'))")
    public void removeCertification(Long doctorId, String certification) {
        log.info("Removing certification from doctor {}: {}", doctorId, certification);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + doctorId));

        doctor.removeCertification(certification);
        doctorRepository.save(doctor);

        auditServiceImpl.logUserAction("CERTIFICATION_REMOVED", doctorId,
                "Certification removed: " + certification);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void setEmergencyContact(Long doctorId, boolean isEmergencyContact) {
        log.info("Setting emergency contact status for doctor {}: {}", doctorId, isEmergencyContact);

        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with ID: " + doctorId));

        doctorRepository.updateEmergencyContactStatus(doctorId, isEmergencyContact);

        auditServiceImpl.logUserAction("EMERGENCY_CONTACT_UPDATED", doctorId,
                "Emergency contact status updated to: " + isEmergencyContact);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "emergency-doctors")
    public List<DoctorDto> getEmergencyContactDoctors() {
        log.debug("Fetching emergency contact doctors");
        return doctorMapper.toDtoList(doctorRepository.findEmergencyContactDoctors());
    }
}
