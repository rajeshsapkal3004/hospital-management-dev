package com.hospital_management.services.userservice;


import com.hospital_management.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PatientService {

    // Registration and basic operations
    PatientDto registerPatient(PatientRegistrationDto registrationDto);
    PatientDto getPatientById(Long id);
    PatientDto getPatientByPatientId(String patientId);
    PatientDto getPatientByInsuranceNumber(String insuranceNumber);

    // Update operations
    PatientDto updatePatient(Long id, PatientRegistrationDto updateDto);
    void updatePhysicalMeasurements(Long patientId, Double height, Double weight);
    void updateInsuranceInfo(Long patientId, String provider, String insuranceNumber);
    void updateEmergencyContact(Long patientId, String name, String phone, String relationship);
    void updateLastVisitDate(Long patientId);

    // Status management
    void activatePatient(Long patientId);
    void deactivatePatient(Long patientId);

    // Medical information management
    void addAllergy(Long patientId, String allergy);
    void removeAllergy(Long patientId, String allergy);
    void addMedicalCondition(Long patientId, String condition);
    void removeMedicalCondition(Long patientId, String condition);
    void addCurrentMedication(Long patientId, String medication);
    void removeCurrentMedication(Long patientId, String medication);

    // Search and filtering
    Page<PatientDto> getAllPatients(Pageable pageable);
    List<PatientDto> getActivePatients();
    List<PatientDto> getPatientsByBloodType(String bloodType);
    List<PatientDto> getPatientsByInsuranceProvider(String provider);
    List<PatientDto> getPatientsByAgeRange(Integer minAge, Integer maxAge);
    List<PatientDto> getPatientsWithAllergy(String allergy);
    List<PatientDto> getPatientsWithMedicalCondition(String condition);
    Page<PatientDto> searchPatients(String patientId, String bloodType, String insuranceProvider,
                                    Integer minAge, Integer maxAge, Boolean active, Pageable pageable);

    // Validation
    boolean existsByPatientId(String patientId);
    boolean existsByInsuranceNumber(String insuranceNumber);
    boolean isPatientIdAvailable(String patientId, Long excludePatientId);

    // Statistics
    long getTotalPatientCount();
    long getActivePatientCount();
    Map<String, Long> getPatientCountByBloodType();
    Map<String, Long> getPatientCountByInsuranceProvider();
    Double getAveragePatientAge();

    // Patient Profile Management

    PatientDto getPatientByUserId(Long userId);
    PatientDto updatePatientProfile(Long userId, PatientUpdateDto updateDto);
    String uploadPatientPhoto(Long userId, MultipartFile photo);

    // Vital Signs
    Page<VitalSignsDto> getPatientVitalSigns(Long patientId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    VitalSignsDto addVitalSigns(Long patientId, VitalSignsDto vitalSignsDto);

    // Allergies
    List<AllergyDto> getPatientAllergies(Long patientId);
    AllergyDto addPatientAllergy(AllergyDto allergyDto);
    AllergyDto updatePatientAllergy(Long patientId, Long allergyId, AllergyDto allergyDto);
    void deletePatientAllergy(Long patientId, Long allergyId);

    // Emergency Contacts
    List<EmergencyContactDto> getPatientEmergencyContacts(Long patientId);
    EmergencyContactDto addEmergencyContact(EmergencyContactDto contactDto);
    EmergencyContactDto updateEmergencyContact(Long patientId, Long contactId, EmergencyContactDto contactDto);
    void deleteEmergencyContact(Long patientId, Long contactId);

    // Insurance
    List<InsuranceDto> getPatientInsurance(Long patientId);
    InsuranceDto addPatientInsurance(InsuranceDto insuranceDto);
    InsuranceDto updatePatientInsurance(Long patientId, Long insuranceId, InsuranceDto insuranceDto);
    void deletePatientInsurance(Long patientId, Long insuranceId);

    // Medical History
    MedicalHistorySummaryDto getPatientMedicalHistory(Long patientId);

    // Notifications
    Page<NotificationDto> getPatientNotifications(Long patientId, Boolean read, Pageable pageable);
    void markNotificationAsRead(Long patientId, Long notificationId);

    // Discharge Summaries
    List<DischargeSummaryDto> getPatientDischargeSummaries(Long patientId);

    // Feedback & Reviews
    FeedbackDto submitPatientFeedback(FeedbackDto feedbackDto);
    ReviewDto submitAppointmentReview(ReviewDto reviewDto);

    // Dashboard
    PatientDashboardDto getPatientDashboard(Long patientId);
}
