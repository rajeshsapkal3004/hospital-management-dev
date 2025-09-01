package com.hospital_management.services.userservice;

import com.hospital_management.dtos.*;
import com.hospital_management.exception.*;
import com.hospital_management.mapper.*;
import com.hospital_management.models.*;
import com.hospital_management.repo.*;
import com.hospital_management.services.auditlogs.AuditService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientMapper patientMapper;
    private final AuditService auditService;


    private final UserRepository userRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final AllergyRepository allergyRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final InsuranceRepository insuranceRepository;
    private final NotificationRepository notificationRepository;
    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabResultRepository labResultRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    private final VitalSignsMapper vitalSignsMapper;
    private final AllergyMapper allergyMapper;
    private final EmergencyContactMapper emergencyContactMapper;
    private final InsuranceMapper insuranceMapper;
    private final NotificationMapper notificationMapper;
    private final DischargeSummaryMapper dischargeSummaryMapper;
    private final FeedbackMapper feedbackMapper;
    private final ReviewMapper reviewMapper;
    private final AppointmentMapper appointmentMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final LabResultMapper labResultMapper;

    private final String uploadPath = "uploads/patient-photos/";

    @Override
    @Transactional
    public PatientDto registerPatient(@Valid PatientRegistrationDto registrationDto) {
        log.info("Registering new patient with email: {}", registrationDto.getEmail());

        // Validation
        validatePatientRegistration(registrationDto);

        try {
            // Create patient entity
            Patient patient = patientMapper.toEntity(registrationDto);
            patient.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Generate patient ID if not provided
            if (patient.getPatientId() == null) {
                patient.setPatientId(generatePatientId());
            }

            // Assign PATIENT role
            Role patientRole = roleRepository.findByName(Role.RoleName.PATIENT)
                    .orElseThrow(() -> new ValidationException("PATIENT role not found"));
            patient.getRoles().add(patientRole);

            Patient savedPatient = patientRepository.save(patient);

            // Audit logging
            auditService.logUserAction("PATIENT_REGISTRATION", savedPatient.getId(),
                    "Patient registered with ID: " + savedPatient.getPatientId());

            log.info("Patient registered successfully with ID: {}", savedPatient.getId());
            return patientMapper.toDto(savedPatient);

        } catch (Exception e) {
            log.error("Error registering patient: {}", e.getMessage(), e);
            throw new ValidationException("Failed to register patient: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "#id")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST') or (hasRole('PATIENT') and #id == authentication.principal.id)")
    public PatientDto getPatientById(Long id) {
        log.debug("Fetching patient by ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with ID: " + id));
        return patientMapper.toDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "#patientId")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public PatientDto getPatientByPatientId(String patientId) {
        log.debug("Fetching patient by patient ID: {}", patientId);
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with patient ID: " + patientId));
        return patientMapper.toDto(patient);
    }

    @Override
    public PatientDto getPatientByInsuranceNumber(String insuranceNumber) {
        return null;
    }

    @Override
    public PatientDto updatePatient(Long id, PatientRegistrationDto updateDto) {
        return null;
    }

    @Override
    public void updatePhysicalMeasurements(Long patientId, Double height, Double weight) {

    }

    @Override
    public void updateInsuranceInfo(Long patientId, String provider, String insuranceNumber) {

    }

    @Override
    public void updateEmergencyContact(Long patientId, String name, String phone, String relationship) {

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or (#patientId == authentication.principal.id and hasRole('PATIENT'))")
    public void addAllergy(Long patientId, String allergy) {
        log.info("Adding allergy to patient {}: {}", patientId, allergy);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with ID: " + patientId));

        patient.addAllergy(allergy);
        patientRepository.save(patient);

        auditService.logUserAction("ALLERGY_ADDED", patientId, "Allergy added: " + allergy);
    }

    @Override
    public void removeAllergy(Long patientId, String allergy) {

    }

    @Override
    public void addMedicalCondition(Long patientId, String condition) {

    }

    @Override
    public void removeMedicalCondition(Long patientId, String condition) {

    }

    @Override
    public void addCurrentMedication(Long patientId, String medication) {

    }

    @Override
    public void removeCurrentMedication(Long patientId, String medication) {

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public void updateLastVisitDate(Long patientId) {
        log.info("Updating last visit date for patient: {}", patientId);

        patientRepository.findById(patientId)
                .orElseThrow(() -> new UserNotFoundException("Patient not found with ID: " + patientId));

        patientRepository.updateLastVisitDate(patientId, LocalDateTime.now());

        auditService.logUserAction("VISIT_RECORDED", patientId, "Patient visit recorded");
    }

    @Override
    public void activatePatient(Long patientId) {

    }

    @Override
    public void deactivatePatient(Long patientId) {

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "active-patients")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public List<PatientDto> getActivePatients() {
        log.debug("Fetching active patients");
        return patientMapper.toDtoList(patientRepository.findByIsActivePatientTrue());
    }

    @Override
    public List<PatientDto> getPatientsByBloodType(String bloodType) {
        return null;
    }

    @Override
    public List<PatientDto> getPatientsByInsuranceProvider(String provider) {
        return null;
    }

    @Override
    public List<PatientDto> getPatientsByAgeRange(Integer minAge, Integer maxAge) {
        return null;
    }

    @Override
    public List<PatientDto> getPatientsWithAllergy(String allergy) {
        return null;
    }

    @Override
    public List<PatientDto> getPatientsWithMedicalCondition(String condition) {
        return null;
    }

    @Override
    public Page<PatientDto> searchPatients(String patientId, String bloodType, String insuranceProvider, Integer minAge, Integer maxAge, Boolean active, Pageable pageable) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientId(String patientId) {
        return patientRepository.existsByPatientId(patientId);
    }

    @Override
    public boolean existsByInsuranceNumber(String insuranceNumber) {
        return false;
    }

    @Override
    public boolean isPatientIdAvailable(String patientId, Long excludePatientId) {
        return false;
    }

    private void validatePatientRegistration(PatientRegistrationDto dto) {
        if (dto.getInsuranceNumber() != null &&
                patientRepository.existsByInsuranceNumber(dto.getInsuranceNumber())) {
            throw new UserAlreadyExistsException("insurance number", dto.getInsuranceNumber());
        }
    }

    private String generatePatientId() {
        // Generate unique patient ID with format PAT + 8 digits
        String prefix = "PAT";
        long count = patientRepository.count() + 1;
        return prefix + String.format("%08d", count);
    }

    // Additional method implementations would follow the same pattern...

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    public Page<PatientDto> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPatientCount() {
        return patientRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActivePatientCount() {
        return patientRepository.countByIsActivePatientTrue();
    }

    @Override
    public Map<String, Long> getPatientCountByBloodType() {
        return null;
    }

    @Override
    public Map<String, Long> getPatientCountByInsuranceProvider() {
        return null;
    }

    @Override
    public Double getAveragePatientAge() {
        return null;
    }

    // Additional implementations for all interface methods...


    @Override
    @Transactional(readOnly = true)
    public PatientDto getPatientByUserId(Long userId) {
        log.debug("Fetching patient by user ID: {}", userId);

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found for user ID: " + userId));

        return patientMapper.toDto(patient);
    }

    @Override
    @Transactional
    public PatientDto updatePatientProfile(Long userId, PatientUpdateDto updateDto) {
        log.info("Updating patient profile for user: {}", userId);

        try {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new PatientNotFoundException("Patient not found for user ID: " + userId));

            // Update basic information
            if (updateDto.getFirstName() != null) {
                patient.setFirstName(updateDto.getFirstName());
            }
            if (updateDto.getLastName() != null) {
                patient.setLastName(updateDto.getLastName());
            }
            if (updateDto.getPhone() != null) {
                patient.setPhone(updateDto.getPhone());
            }
            if (updateDto.getGender() != null) {
                patient.setGender(updateDto.getGender());
            }
            if (updateDto.getAddress() != null) {
                patient.setAddress(updateDto.getAddress());
            }
            if (updateDto.getCity() != null) {
                patient.setCity(updateDto.getCity());
            }
            if (updateDto.getState() != null) {
                patient.setState(updateDto.getState());
            }
            if (updateDto.getCountry() != null) {
                patient.setCountry(updateDto.getCountry());
            }
            if (updateDto.getPostalCode() != null) {
                patient.setPostalCode(updateDto.getPostalCode());
            }

            // Update patient-specific fields
            if (updateDto.getDateOfBirth() != null) {
                patient.setDateOfBirth(updateDto.getDateOfBirth());
            }
            if (updateDto.getBloodGroup() != null) {
                patient.setBloodType(updateDto.getBloodGroup());
            }
            if (updateDto.getHeight() != null) {
                patient.setHeightCm(updateDto.getHeight());
            }
            if (updateDto.getWeight() != null) {
                patient.setWeightKg(updateDto.getWeight());
            }
            if (updateDto.getOccupation() != null) {
                patient.setOccupation(updateDto.getOccupation());
            }
            if (updateDto.getEmployer() != null) {
                patient.setEmployer(updateDto.getEmployer());
            }
            if (updateDto.getMedicalHistory() != null) {
                patient.setMedicalHistory(updateDto.getMedicalHistory());
            }
            if (updateDto.getFamilyHistory() != null) {
                patient.setFamilyHistory(updateDto.getFamilyHistory());
            }
            if (updateDto.getSocialHistory() != null) {
                patient.setSocialHistory(updateDto.getSocialHistory());
            }
            if (updateDto.getMaritalStatus() != null) {
                patient.setMaritalStatus(updateDto.getMaritalStatus());
            }
            if (updateDto.getPreferredLanguage() != null) {
                patient.setPreferredLanguage(updateDto.getPreferredLanguage());
            }
            if (updateDto.getReligion() != null) {
                patient.setReligion(updateDto.getReligion());
            }
            if (updateDto.getSmokingStatus() != null) {
                patient.setSmokingStatus(updateDto.getSmokingStatus());
            }
            if (updateDto.getDrinkingStatus() != null) {
                patient.setDrinkingStatus(updateDto.getDrinkingStatus());
            }

            Patient saved = patientRepository.save(patient);
            return patientMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating patient profile", e);
            throw new RuntimeException("Failed to update patient profile: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String uploadPatientPhoto(Long userId, MultipartFile photo) {
        log.info("Uploading photo for patient user: {}", userId);

        try {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new PatientNotFoundException("Patient not found for user ID: " + userId));

            // Validate file
            if (photo.isEmpty()) {
                throw new IllegalArgumentException("Photo file is empty");
            }

            String fileName = "patient_" + patient.getId() + "_" + System.currentTimeMillis()
                    + getFileExtension(photo.getOriginalFilename());

            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Save file
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update patient with photo URL
            String photoUrl = "/uploads/patient-photos/" + fileName;
            patient.setProfilePhotoUrl(photoUrl);
            patientRepository.save(patient);

            return photoUrl;

        } catch (IOException e) {
            log.error("Error uploading patient photo", e);
            throw new RuntimeException("Failed to upload photo: " + e.getMessage());
        }
    }

    // ===============================
    // VITAL SIGNS
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public Page<VitalSignsDto> getPatientVitalSigns(Long patientId, LocalDate startDate,
                                                    LocalDate endDate, Pageable pageable) {
        log.debug("Fetching vital signs for patient: {}", patientId);

        Specification<VitalSigns> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patientId"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("recordedDate")), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("recordedDate")), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<VitalSigns> vitalSigns = vitalSignsRepository.findAll(spec, pageable);
        return vitalSigns.map(vitalSignsMapper::toDto);
    }

    @Override
    @Transactional
    public VitalSignsDto addVitalSigns(Long patientId, VitalSignsDto vitalSignsDto) {
        log.info("Adding vital signs for patient: {}", patientId);

        try {
            VitalSigns vitalSigns = VitalSigns.builder()
                    .patientId(patientId)
                    .recordedDate(LocalDateTime.now())
                    .temperature(vitalSignsDto.getTemperature())
                    .systolicPressure(vitalSignsDto.getSystolicPressure())
                    .diastolicPressure(vitalSignsDto.getDiastolicPressure())
                    .heartRate(vitalSignsDto.getHeartRate())
                    .respiratoryRate(vitalSignsDto.getRespiratoryRate())
                    .oxygenSaturation(vitalSignsDto.getOxygenSaturation())
                    .weight(vitalSignsDto.getWeight())
                    .height(vitalSignsDto.getHeight())
                    .painLevel(vitalSignsDto.getPainLevel())
                    .consciousness(vitalSignsDto.getConsciousness())
                    .notes(vitalSignsDto.getNotes())
                    .recordedBy(vitalSignsDto.getRecordedBy())
                    .recordedByType(vitalSignsDto.getRecordedByType())
                    .build();

            VitalSigns saved = vitalSignsRepository.save(vitalSigns);
            return vitalSignsMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error adding vital signs", e);
            throw new RuntimeException("Failed to add vital signs: " + e.getMessage());
        }
    }

    // ===============================
    // ALLERGIES
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public List<AllergyDto> getPatientAllergies(Long patientId) {
        log.debug("Fetching allergies for patient: {}", patientId);

        List<Allergy> allergies = allergyRepository.findByPatientIdAndIsActiveTrue(patientId);
        return allergies.stream()
                .map(allergyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AllergyDto addPatientAllergy(AllergyDto allergyDto) {
        log.info("Adding allergy for patient: {}", allergyDto.getPatientId());

        try {
            Allergy allergy = Allergy.builder()
                    .patientId(allergyDto.getPatientId())
                    .allergen(allergyDto.getAllergen())
                    .allergyType(allergyDto.getAllergyType())
                    .severity(allergyDto.getSeverity())
                    .reaction(allergyDto.getReaction())
                    .notes(allergyDto.getNotes())
                    .onsetDate(allergyDto.getOnsetDate())
                    .isActive(true)
                    .reportedBy(allergyDto.getReportedBy())
                    .reportedDate(LocalDateTime.now())
                    .build();

            Allergy saved = allergyRepository.save(allergy);
            return allergyMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error adding patient allergy", e);
            throw new RuntimeException("Failed to add allergy: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AllergyDto updatePatientAllergy(Long patientId, Long allergyId, AllergyDto allergyDto) {
        log.info("Updating allergy {} for patient: {}", allergyId, patientId);

        try {
            Allergy allergy = allergyRepository.findByIdAndPatientId(allergyId, patientId)
                    .orElseThrow(() -> new AllergyNotFoundException(allergyId));

            allergy.setAllergen(allergyDto.getAllergen());
            allergy.setAllergyType(allergyDto.getAllergyType());
            allergy.setSeverity(allergyDto.getSeverity());
            allergy.setReaction(allergyDto.getReaction());
            allergy.setNotes(allergyDto.getNotes());
            allergy.setOnsetDate(allergyDto.getOnsetDate());
            allergy.setIsActive(allergyDto.getIsActive());

            Allergy saved = allergyRepository.save(allergy);
            return allergyMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating patient allergy", e);
            throw new RuntimeException("Failed to update allergy: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deletePatientAllergy(Long patientId, Long allergyId) {
        log.info("Deleting allergy {} for patient: {}", allergyId, patientId);

        try {
            Allergy allergy = allergyRepository.findByIdAndPatientId(allergyId, patientId)
                    .orElseThrow(() -> new AllergyNotFoundException(allergyId));

            allergy.setIsActive(false); // Soft delete
            allergyRepository.save(allergy);

        } catch (Exception e) {
            log.error("Error deleting patient allergy", e);
            throw new RuntimeException("Failed to delete allergy: " + e.getMessage());
        }
    }

    // ===============================
    // EMERGENCY CONTACTS
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyContactDto> getPatientEmergencyContacts(Long patientId) {
        log.debug("Fetching emergency contacts for patient: {}", patientId);

        List<EmergencyContact> contacts = emergencyContactRepository.findByPatientIdAndIsActiveTrue(patientId);
        return contacts.stream()
                .map(emergencyContactMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmergencyContactDto addEmergencyContact(EmergencyContactDto contactDto) {
        log.info("Adding emergency contact for patient: {}", contactDto.getPatientId());

        try {
            EmergencyContact contact = EmergencyContact.builder()
                    .patientId(contactDto.getPatientId())
                    .contactName(contactDto.getContactName())
                    .relationship(contactDto.getRelationship())
                    .phoneNumber(contactDto.getPhoneNumber())
                    .email(contactDto.getEmail())
                    .address(contactDto.getAddress())
                    .isPrimary(contactDto.getIsPrimary())
                    .isActive(true)
                    .notes(contactDto.getNotes())
                    .build();

            EmergencyContact saved = emergencyContactRepository.save(contact);
            return emergencyContactMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error adding emergency contact", e);
            throw new RuntimeException("Failed to add emergency contact: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public EmergencyContactDto updateEmergencyContact(Long patientId, Long contactId,
                                                      EmergencyContactDto contactDto) {
        log.info("Updating emergency contact {} for patient: {}", contactId, patientId);

        try {
            EmergencyContact contact = emergencyContactRepository.findByIdAndPatientId(contactId, patientId)
                    .orElseThrow(() -> new EmergencyContactNotFoundException(contactId));

            contact.setContactName(contactDto.getContactName());
            contact.setRelationship(contactDto.getRelationship());
            contact.setPhoneNumber(contactDto.getPhoneNumber());
            contact.setEmail(contactDto.getEmail());
            contact.setAddress(contactDto.getAddress());
            contact.setIsPrimary(contactDto.getIsPrimary());
            contact.setNotes(contactDto.getNotes());

            EmergencyContact saved = emergencyContactRepository.save(contact);
            return emergencyContactMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating emergency contact", e);
            throw new RuntimeException("Failed to update emergency contact: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteEmergencyContact(Long patientId, Long contactId) {
        log.info("Deleting emergency contact {} for patient: {}", contactId, patientId);

        try {
            EmergencyContact contact = emergencyContactRepository.findByIdAndPatientId(contactId, patientId)
                    .orElseThrow(() -> new EmergencyContactNotFoundException(contactId));

            contact.setIsActive(false); // Soft delete
            emergencyContactRepository.save(contact);

        } catch (Exception e) {
            log.error("Error deleting emergency contact", e);
            throw new RuntimeException("Failed to delete emergency contact: " + e.getMessage());
        }
    }

    // ===============================
    // INSURANCE
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceDto> getPatientInsurance(Long patientId) {
        log.debug("Fetching insurance for patient: {}", patientId);

        List<Insurance> insurance = insuranceRepository.findByPatientIdAndIsActiveTrue(patientId);
        return insurance.stream()
                .map(insuranceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InsuranceDto addPatientInsurance(InsuranceDto insuranceDto) {
        log.info("Adding insurance for patient: {}", insuranceDto.getPatientId());

        try {
            Insurance insurance = Insurance.builder()
                    .patientId(insuranceDto.getPatientId())
                    .insuranceProvider(insuranceDto.getInsuranceProvider())
                    .policyNumber(insuranceDto.getPolicyNumber())
                    .groupNumber(insuranceDto.getGroupNumber())
                    .planName(insuranceDto.getPlanName())
                    .subscriberName(insuranceDto.getSubscriberName())
                    .subscriberId(insuranceDto.getSubscriberId())
                    .effectiveDate(insuranceDto.getEffectiveDate())
                    .expirationDate(insuranceDto.getExpirationDate())
                    .isPrimary(insuranceDto.getIsPrimary())
                    .isActive(true)
                    .copayAmount(insuranceDto.getCopayAmount())
                    .deductibleAmount(insuranceDto.getDeductibleAmount())
                    .coverageDetails(insuranceDto.getCoverageDetails())
                    .notes(insuranceDto.getNotes())
                    .build();

            Insurance saved = insuranceRepository.save(insurance);
            return insuranceMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error adding patient insurance", e);
            throw new RuntimeException("Failed to add insurance: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public InsuranceDto updatePatientInsurance(Long patientId, Long insuranceId, InsuranceDto insuranceDto) {
        log.info("Updating insurance {} for patient: {}", insuranceId, patientId);

        try {
            Insurance insurance = insuranceRepository.findByIdAndPatientId(insuranceId, patientId)
                    .orElseThrow(() -> new InsuranceNotFoundException(insuranceId));

            insurance.setInsuranceProvider(insuranceDto.getInsuranceProvider());
            insurance.setPolicyNumber(insuranceDto.getPolicyNumber());
            insurance.setGroupNumber(insuranceDto.getGroupNumber());
            insurance.setPlanName(insuranceDto.getPlanName());
            insurance.setSubscriberName(insuranceDto.getSubscriberName());
            insurance.setSubscriberId(insuranceDto.getSubscriberId());
            insurance.setEffectiveDate(insuranceDto.getEffectiveDate());
            insurance.setExpirationDate(insuranceDto.getExpirationDate());
            insurance.setIsPrimary(insuranceDto.getIsPrimary());
            insurance.setCopayAmount(insuranceDto.getCopayAmount());
            insurance.setDeductibleAmount(insuranceDto.getDeductibleAmount());
            insurance.setCoverageDetails(insuranceDto.getCoverageDetails());
            insurance.setNotes(insuranceDto.getNotes());

            Insurance saved = insuranceRepository.save(insurance);
            return insuranceMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating patient insurance", e);
            throw new RuntimeException("Failed to update insurance: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deletePatientInsurance(Long patientId, Long insuranceId) {
        log.info("Deleting insurance {} for patient: {}", insuranceId, patientId);

        try {
            Insurance insurance = insuranceRepository.findByIdAndPatientId(insuranceId, patientId)
                    .orElseThrow(() -> new InsuranceNotFoundException(insuranceId));

            insurance.setIsActive(false); // Soft delete
            insuranceRepository.save(insurance);

        } catch (Exception e) {
            log.error("Error deleting patient insurance", e);
            throw new RuntimeException("Failed to delete insurance: " + e.getMessage());
        }
    }

    // ===============================
    // MEDICAL HISTORY
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public MedicalHistorySummaryDto getPatientMedicalHistory(Long patientId) {
        log.debug("Fetching medical history summary for patient: {}", patientId);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);
            List<Allergy> allergies = allergyRepository.findByPatientIdAndIsActiveTrue(patientId);

            // Build summary
            MedicalHistorySummaryDto summary = MedicalHistorySummaryDto.builder()
                    .patientId(patientId)
                    .patientName(patient.getFirstName() + " " + patient.getLastName())
                    .totalRecords(records.size())
                    .firstVisit(records.isEmpty() ? null : records.get(records.size() - 1).getRecordDate())
                    .lastVisit(records.isEmpty() ? null : records.get(0).getRecordDate())
                    .allergies(allergies.stream().map(Allergy::getAllergen).collect(Collectors.toList()))
                    .bloodType(patient.getBloodType())
                    .build();

            // Extract chronic conditions and diagnosis frequency
            Map<String, Integer> diagnosisFreq = new HashMap<>();
            List<String> chronicConditions = new ArrayList<>();

            for (MedicalRecord record : records) {
                if (record.getDiagnosis() != null) {
                    diagnosisFreq.merge(record.getDiagnosis(), 1, Integer::sum);

                    // Check for chronic conditions
                    String diagnosis = record.getDiagnosis().toLowerCase();
                    if (diagnosis.contains("diabetes") || diagnosis.contains("hypertension") ||
                            diagnosis.contains("chronic") || diagnosis.contains("asthma")) {
                        if (!chronicConditions.contains(record.getDiagnosis())) {
                            chronicConditions.add(record.getDiagnosis());
                        }
                    }
                }
            }

            summary.setDiagnosisFrequency(diagnosisFreq);
            summary.setChronicConditions(chronicConditions);

            return summary;

        } catch (Exception e) {
            log.error("Error fetching patient medical history", e);
            throw new RuntimeException("Failed to fetch medical history: " + e.getMessage());
        }
    }

    // ===============================
    // NOTIFICATIONS
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getPatientNotifications(Long patientId, Boolean read, Pageable pageable) {
        log.debug("Fetching notifications for patient: {}", patientId);

        Specification<Notification> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("userId"), patientId));

            if (read != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRead"), read));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Notification> notifications = notificationRepository.findAll(spec, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long patientId, Long notificationId) {
        log.info("Marking notification {} as read for patient: {}", notificationId, patientId);

        try {
            Notification notification = notificationRepository.findByIdAndUserId(notificationId, patientId)
                    .orElseThrow(() -> new NotificationNotFoundException(notificationId));

            notification.setIsRead(true);
            notification.setReadDate(LocalDateTime.now());
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Error marking notification as read", e);
            throw new RuntimeException("Failed to mark notification as read: " + e.getMessage());
        }
    }

    // ===============================
    // DISCHARGE SUMMARIES
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public List<DischargeSummaryDto> getPatientDischargeSummaries(Long patientId) {
        log.debug("Fetching discharge summaries for patient: {}", patientId);

        List<DischargeSummary> summaries = dischargeSummaryRepository.findByPatientIdOrderByDischargeDateDesc(patientId);
        return summaries.stream()
                .map(dischargeSummaryMapper::toDto)
                .collect(Collectors.toList());
    }

    // ===============================
    // FEEDBACK & REVIEWS
    // ===============================

    @Override
    @Transactional
    public FeedbackDto submitPatientFeedback(FeedbackDto feedbackDto) {
        log.info("Submitting feedback from patient: {}", feedbackDto.getPatientId());

        try {
            Feedback feedback = Feedback.builder()
                    .patientId(feedbackDto.getPatientId())
                    .subject(feedbackDto.getSubject())
                    .message(feedbackDto.getMessage())
                    .category(feedbackDto.getCategory())
                    .rating(feedbackDto.getRating())
                    .status("SUBMITTED")
                    .submittedDate(LocalDateTime.now())
                    .build();

            Feedback saved = feedbackRepository.save(feedback);
            return feedbackMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error submitting patient feedback", e);
            throw new RuntimeException("Failed to submit feedback: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReviewDto submitAppointmentReview(ReviewDto reviewDto) {
        log.info("Submitting appointment review from patient: {}", reviewDto.getPatientId());

        try {
            Review review = Review.builder()
                    .patientId(reviewDto.getPatientId())
                    .appointmentId(reviewDto.getAppointmentId())
                    .doctorId(reviewDto.getDoctorId())
                    .rating(reviewDto.getRating())
                    .reviewText(reviewDto.getReviewText())
                    .recommendations(reviewDto.getRecommendations())
                    .wouldRecommend(reviewDto.getWouldRecommend())
                    .submittedDate(LocalDateTime.now())
                    .isVerified(false)
                    .isPublic(true)
                    .build();

            Review saved = reviewRepository.save(review);
            return reviewMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error submitting appointment review", e);
            throw new RuntimeException("Failed to submit review: " + e.getMessage());
        }
    }

    // ===============================
    // DASHBOARD
    // ===============================

    @Override
    @Transactional(readOnly = true)
    public PatientDashboardDto getPatientDashboard(Long patientId) {
        log.debug("Fetching dashboard data for patient: {}", patientId);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            // Get upcoming appointments
            List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingAppointments(
                    patientId, LocalDateTime.now(), LocalDateTime.now().plusDays(30));

            // Get active prescriptions
            List<Prescription> activePrescriptions = prescriptionRepository.findActivePrescriptions(patientId);

            // Get recent lab results
            List<LabResult> recentLabResults = labResultRepository.findRecentResultsForPatient(patientId)
                    .stream().limit(5).collect(Collectors.toList());

            // Get recent notifications
            List<Notification> recentNotifications = notificationRepository.findRecentNotificationsForUser(patientId)
                    .stream().limit(10).collect(Collectors.toList());

            // Get latest vital signs
            VitalSigns latestVitalSigns = vitalSignsRepository.findLatestForPatient(patientId).orElse(null);

            // Build stats
            PatientDashboardDto.DashboardStatsDto stats = PatientDashboardDto.DashboardStatsDto.builder()
                    .totalAppointments(appointmentRepository.countByPatientId(patientId))
                    .completedAppointments(appointmentRepository.countCompletedByPatientId(patientId))
                    .activePrescriptions(activePrescriptions.size())
                    .pendingLabTests(labResultRepository.countPendingForPatient(patientId))
                    .unreadNotifications(notificationRepository.countUnreadForUser(patientId))
                    .build();

            return PatientDashboardDto.builder()
                    .patient(patientMapper.toDto(patient))
                    .upcomingAppointments(upcomingAppointments.stream().map(appointmentMapper::toDto).collect(Collectors.toList()))
                    .activePrescriptions(activePrescriptions.stream().map(prescriptionMapper::toDto).collect(Collectors.toList()))
                    .recentLabResults(recentLabResults.stream().map(labResultMapper::toDto).collect(Collectors.toList()))
                    .recentNotifications(recentNotifications.stream().map(notificationMapper::toDto).collect(Collectors.toList()))
                    .latestVitalSigns(latestVitalSigns != null ? vitalSignsMapper.toDto(latestVitalSigns) : null)
                    .stats(stats)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching patient dashboard", e);
            throw new RuntimeException("Failed to fetch dashboard data: " + e.getMessage());
        }
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg"; // Default extension
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }





}
