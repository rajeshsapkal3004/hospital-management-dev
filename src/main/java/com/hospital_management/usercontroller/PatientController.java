package com.hospital_management.usercontroller;


import com.hospital_management.config.UserPrincipal;
import com.hospital_management.dtos.*;

import com.hospital_management.services.appointment.AppointmentService;
import com.hospital_management.services.billing.BillingService;
import com.hospital_management.services.lab.LabResultService;
import com.hospital_management.services.medical.MedicalRecordService;
import com.hospital_management.services.prescription.PrescriptionService;
import com.hospital_management.services.userservice.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Patient Management", description = "Patient operations and healthcare services")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;
    private final PrescriptionService prescriptionService;
    private final LabResultService labResultService;

    // ===============================
    // PATIENT REGISTRATION & PROFILE
    // ===============================

    @PostMapping("/register")
    @Operation(summary = "Register new patient", description = "Public patient self-registration")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody PatientRegistrationDto registrationDto) {
        try {
            log.info("Patient registration: {}", registrationDto.getEmail());
            PatientDto patient = patientService.registerPatient(registrationDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Registration successful. Please verify your email to complete registration."));

        } catch (Exception e) {
            log.error("Patient registration failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient profile")
    public ResponseEntity<PatientDto> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Fetching profile for patient: {}", currentUser.getUsername());
        PatientDto patient = patientService.getPatientByUserId(currentUser.getId());
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update patient profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody PatientUpdateDto updateDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Updating profile for patient: {}", currentUser.getUsername());
            PatientDto updatedPatient = patientService.updatePatientProfile(currentUser.getId(), updateDto);
            return ResponseEntity.ok(updatedPatient);

        } catch (Exception e) {
            log.error("Profile update failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Profile update failed: " + e.getMessage()));
        }
    }

    @PostMapping("/profile/upload-photo")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Upload patient photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("photo") MultipartFile photo,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Uploading photo for patient: {}", currentUser.getUsername());
            String photoUrl = patientService.uploadPatientPhoto(currentUser.getId(), photo);

            return ResponseEntity.ok(Map.of(
                    "message", "Photo uploaded successfully",
                    "photoUrl", photoUrl
            ));

        } catch (Exception e) {
            log.error("Photo upload failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Photo upload failed: " + e.getMessage()));
        }
    }

    // ===============================
    // APPOINTMENT MANAGEMENT
    // ===============================

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book new appointment")
    public ResponseEntity<?> bookAppointment(
            @Valid @RequestBody BookAppointmentDto appointmentDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Booking appointment for patient: {}", currentUser.getUsername());
            appointmentDto.setPatientId(currentUser.getId());
            AppointmentDto appointment = appointmentService.bookAppointment(appointmentDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);

        } catch (Exception e) {
            log.error("Appointment booking failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Appointment booking failed: " + e.getMessage()));
        }
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient appointments")
    public ResponseEntity<Page<AppointmentDto>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "appointmentDateTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching appointments for patient: {}", currentUser.getUsername());

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AppointmentDto> appointments = appointmentService.getPatientAppointments(
                currentUser.getId(), startDate, endDate, status, pageable);

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get appointment details")
    public ResponseEntity<AppointmentDto> getAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching appointment {} for patient: {}", appointmentId, currentUser.getUsername());
        AppointmentDto appointment = appointmentService.getPatientAppointment(currentUser.getId(), appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Reschedule appointment")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody RescheduleAppointmentDto rescheduleDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Rescheduling appointment {} for patient: {}", appointmentId, currentUser.getUsername());
            AppointmentDto appointment = appointmentService.rescheduleAppointment(
                    appointmentId, currentUser.getId(), rescheduleDto);
            return ResponseEntity.ok(appointment);

        } catch (Exception e) {
            log.error("Appointment rescheduling failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Rescheduling failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Cancel appointment")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Cancelling appointment {} for patient: {}", appointmentId, currentUser.getUsername());
            appointmentService.cancelAppointment(appointmentId, currentUser.getId(), reason);

            return ResponseEntity.ok(new MessageResponse("Appointment cancelled successfully"));

        } catch (Exception e) {
            log.error("Appointment cancellation failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Cancellation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/appointments/upcoming")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get upcoming appointments")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointments(
            @RequestParam(defaultValue = "7") @Min(1) @Max(30) int days,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching upcoming appointments for patient: {}", currentUser.getUsername());
        List<AppointmentDto> appointments = appointmentService.getUpcomingAppointments(currentUser.getId(), days);
        return ResponseEntity.ok(appointments);
    }

    // ===============================
    // MEDICAL RECORDS
    // ===============================

    @GetMapping("/medical-records")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient medical records")
    public ResponseEntity<Page<MedicalRecordDto>> getMedicalRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String recordType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching medical records for patient: {}", currentUser.getUsername());

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MedicalRecordDto> records = medicalRecordService.getPatientMedicalRecords(
                currentUser.getId(), startDate, endDate, recordType, pageable);

        return ResponseEntity.ok(records);
    }

    @GetMapping("/medical-records/{recordId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get medical record details")
    public ResponseEntity<MedicalRecordDto> getMedicalRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching medical record {} for patient: {}", recordId, currentUser.getUsername());
        MedicalRecordDto record = medicalRecordService.getPatientMedicalRecord(currentUser.getId(), recordId);
        return ResponseEntity.ok(record);
    }

    @PostMapping("/medical-records/{recordId}/download")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Download medical record")
    public ResponseEntity<?> downloadMedicalRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Downloading medical record {} for patient: {}", recordId, currentUser.getUsername());
            String downloadUrl = medicalRecordService.generateDownloadUrl(currentUser.getId(), recordId);

            return ResponseEntity.ok(Map.of(
                    "message", "Download link generated successfully",
                    "downloadUrl", downloadUrl,
                    "expiresIn", "1 hour"
            ));

        } catch (Exception e) {
            log.error("Medical record download failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Download failed: " + e.getMessage()));
        }
    }

    // ===============================
    // PRESCRIPTIONS
    // ===============================

    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient prescriptions")
    public ResponseEntity<Page<PrescriptionDto>> getPrescriptions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching prescriptions for patient: {}", currentUser.getUsername());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prescribedDate"));

        Page<PrescriptionDto> prescriptions = prescriptionService.getPatientPrescriptions(
                currentUser.getId(), startDate, endDate, status, pageable);

        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get prescription details")
    public ResponseEntity<PrescriptionDto> getPrescription(
            @PathVariable Long prescriptionId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching prescription {} for patient: {}", prescriptionId, currentUser.getUsername());
        PrescriptionDto prescription = prescriptionService.getPatientPrescription(currentUser.getId(), prescriptionId);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/prescriptions/active")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get active prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getActivePrescriptions(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching active prescriptions for patient: {}", currentUser.getUsername());
        List<PrescriptionDto> prescriptions = prescriptionService.getActivePrescriptions(currentUser.getId());
        return ResponseEntity.ok(prescriptions);
    }

    // ===============================
    // LAB RESULTS
    // ===============================

    @GetMapping("/lab-results")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient lab results")
    public ResponseEntity<Page<LabResultDto>> getLabResults(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String testType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching lab results for patient: {}", currentUser.getUsername());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "testDate"));

        Page<LabResultDto> results = labResultService.getPatientLabResults(
                currentUser.getId(), startDate, endDate, testType, pageable);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/lab-results/{resultId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get lab result details")
    public ResponseEntity<LabResultDto> getLabResult(
            @PathVariable Long resultId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching lab result {} for patient: {}", resultId, currentUser.getUsername());
        LabResultDto result = labResultService.getPatientLabResult(currentUser.getId(), resultId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/lab-results/pending")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get pending lab tests")
    public ResponseEntity<List<LabTestDto>> getPendingLabTests(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching pending lab tests for patient: {}", currentUser.getUsername());
        List<LabTestDto> tests = labResultService.getPendingLabTests(currentUser.getId());
        return ResponseEntity.ok(tests);
    }

    // ===============================
    // BILLING & PAYMENTS
    // ===============================

    @GetMapping("/bills")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient bills")
    public ResponseEntity<Page<BillDto>> getBills(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching bills for patient: {}", currentUser.getUsername());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "billDate"));

        Page<BillDto> bills = billingService.getPatientBills(
                currentUser.getId(), startDate, endDate, status, pageable);

        return ResponseEntity.ok(bills);
    }

    @GetMapping("/bills/{billId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get bill details")
    public ResponseEntity<BillDto> getBill(
            @PathVariable Long billId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching bill {} for patient: {}", billId, currentUser.getUsername());
        BillDto bill = billingService.getPatientBill(currentUser.getId(), billId);
        return ResponseEntity.ok(bill);
    }

    @PostMapping("/bills/{billId}/pay")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Make payment for bill")
    public ResponseEntity<?> makePayment(
            @PathVariable Long billId,
            @Valid @RequestBody PaymentDto paymentDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Processing payment for bill {} by patient: {}", billId, currentUser.getUsername());
            paymentDto.setPatientId(currentUser.getId());
            PaymentResultDto result = billingService.processPayment(billId, paymentDto);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Payment processing failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Payment failed: " + e.getMessage()));
        }
    }

    @GetMapping("/bills/outstanding")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get outstanding bills")
    public ResponseEntity<List<BillDto>> getOutstandingBills(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching outstanding bills for patient: {}", currentUser.getUsername());
        List<BillDto> bills = billingService.getOutstandingBills(currentUser.getId());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get payment history")
    public ResponseEntity<Page<PaymentHistoryDto>> getPaymentHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching payment history for patient: {}", currentUser.getUsername());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));

        Page<PaymentHistoryDto> payments = billingService.getPatientPaymentHistory(
                currentUser.getId(), startDate, endDate, pageable);

        return ResponseEntity.ok(payments);
    }

    // ===============================
    // HEALTH RECORDS
    // ===============================

    @GetMapping("/vital-signs")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get vital signs history")
    public ResponseEntity<Page<VitalSignsDto>> getVitalSigns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching vital signs for patient: {}", currentUser.getUsername());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedDate"));

        Page<VitalSignsDto> vitalSigns = patientService.getPatientVitalSigns(
                currentUser.getId(), startDate, endDate, pageable);


        return ResponseEntity.ok(vitalSigns);
    }

    @GetMapping("/allergies")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient allergies")
    public ResponseEntity<List<AllergyDto>> getAllergies(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Fetching allergies for patient: {}", currentUser.getUsername());
        List<AllergyDto> allergies = patientService.getPatientAllergies(currentUser.getId());
        return ResponseEntity.ok(allergies);
    }

    @PostMapping("/allergies")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Add patient allergy")
    public ResponseEntity<?> addAllergy(
            @Valid @RequestBody AllergyDto allergyDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Adding allergy for patient: {}", currentUser.getUsername());
            allergyDto.setPatientId(currentUser.getId());
            AllergyDto allergy = patientService.addPatientAllergy(allergyDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(allergy);

        } catch (Exception e) {
            log.error("Adding allergy failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Adding allergy failed: " + e.getMessage()));
        }
    }

    @GetMapping("/medical-history")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get medical history summary")
    public ResponseEntity<MedicalHistorySummaryDto> getMedicalHistory(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching medical history for patient: {}", currentUser.getUsername());
        MedicalHistorySummaryDto history = patientService.getPatientMedicalHistory(currentUser.getId());
        return ResponseEntity.ok(history);
    }

    // ===============================
    // EMERGENCY CONTACTS
    // ===============================

    @GetMapping("/emergency-contacts")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get emergency contacts")
    public ResponseEntity<List<EmergencyContactDto>> getEmergencyContacts(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching emergency contacts for patient: {}", currentUser.getUsername());
        List<EmergencyContactDto> contacts = patientService.getPatientEmergencyContacts(currentUser.getId());
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/emergency-contacts")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Add emergency contact")
    public ResponseEntity<?> addEmergencyContact(
            @Valid @RequestBody EmergencyContactDto contactDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Adding emergency contact for patient: {}", currentUser.getUsername());
            contactDto.setPatientId(currentUser.getId());
            EmergencyContactDto contact = patientService.addEmergencyContact(contactDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(contact);

        } catch (Exception e) {
            log.error("Adding emergency contact failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Adding contact failed: " + e.getMessage()));
        }
    }

    @PutMapping("/emergency-contacts/{contactId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update emergency contact")
    public ResponseEntity<?> updateEmergencyContact(
            @PathVariable Long contactId,
            @Valid @RequestBody EmergencyContactDto contactDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Updating emergency contact {} for patient: {}", contactId, currentUser.getUsername());
            EmergencyContactDto contact = patientService.updateEmergencyContact(
                    currentUser.getId(), contactId, contactDto);
            return ResponseEntity.ok(contact);

        } catch (Exception e) {
            log.error("Updating emergency contact failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Update failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/emergency-contacts/{contactId}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Delete emergency contact")
    public ResponseEntity<?> deleteEmergencyContact(
            @PathVariable Long contactId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Deleting emergency contact {} for patient: {}", contactId, currentUser.getUsername());
            patientService.deleteEmergencyContact(currentUser.getId(), contactId);

            return ResponseEntity.ok(new MessageResponse("Emergency contact deleted successfully"));

        } catch (Exception e) {
            log.error("Deleting emergency contact failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Deletion failed: " + e.getMessage()));
        }
    }

    // ===============================
    // INSURANCE INFORMATION
    // ===============================

    @GetMapping("/insurance")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get insurance information")
    public ResponseEntity<List<InsuranceDto>> getInsuranceInfo(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching insurance info for patient: {}", currentUser.getUsername());
        List<InsuranceDto> insurance = patientService.getPatientInsurance(currentUser.getId());
        return ResponseEntity.ok(insurance);
    }

    @PostMapping("/insurance")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Add insurance information")
    public ResponseEntity<?> addInsurance(
            @Valid @RequestBody InsuranceDto insuranceDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Adding insurance for patient: {}", currentUser.getUsername());
            insuranceDto.setPatientId(currentUser.getId());
            InsuranceDto insurance = patientService.addPatientInsurance(insuranceDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(insurance);

        } catch (Exception e) {
            log.error("Adding insurance failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Adding insurance failed: " + e.getMessage()));
        }
    }

    // ===============================
    // NOTIFICATIONS
    // ===============================

    @GetMapping("/notifications")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient notifications")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching notifications for patient: {}", currentUser.getUsername());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<NotificationDto> notifications = patientService.getPatientNotifications(
                currentUser.getId(), read, pageable);

        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/notifications/{notificationId}/read")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<?> markNotificationAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Marking notification {} as read for patient: {}", notificationId, currentUser.getUsername());
            patientService.markNotificationAsRead(currentUser.getId(), notificationId);

            return ResponseEntity.ok(new MessageResponse("Notification marked as read"));

        } catch (Exception e) {
            log.error("Marking notification as read failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Operation failed: " + e.getMessage()));
        }
    }

    // ===============================
    // DISCHARGE SUMMARIES
    // ===============================

    @GetMapping("/discharge-summaries")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get discharge summaries")
    public ResponseEntity<List<DischargeSummaryDto>> getDischargeSummaries(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("Fetching discharge summaries for patient: {}", currentUser.getUsername());
        List<DischargeSummaryDto> summaries = patientService.getPatientDischargeSummaries(currentUser.getId());
        return ResponseEntity.ok(summaries);
    }

    // ===============================
    // FEEDBACK & REVIEWS
    // ===============================

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Submit feedback")
    public ResponseEntity<?> submitFeedback(
            @Valid @RequestBody FeedbackDto feedbackDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Submitting feedback from patient: {}", currentUser.getUsername());
            feedbackDto.setPatientId(currentUser.getId());
            FeedbackDto feedback = patientService.submitPatientFeedback(feedbackDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(feedback);

        } catch (Exception e) {
            log.error("Submitting feedback failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Feedback submission failed: " + e.getMessage()));
        }
    }

    @PostMapping("/appointments/{appointmentId}/review")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Submit appointment review")
    public ResponseEntity<?> submitAppointmentReview(
            @PathVariable Long appointmentId,
            @Valid @RequestBody ReviewDto reviewDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            log.info("Submitting review for appointment {} from patient: {}",
                    appointmentId, currentUser.getUsername());
            reviewDto.setPatientId(currentUser.getId());
            reviewDto.setAppointmentId(appointmentId);
            ReviewDto review = patientService.submitAppointmentReview(reviewDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(review);

        } catch (Exception e) {
            log.error("Submitting review failed", e);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Review submission failed: " + e.getMessage()));
        }
    }

    // ===============================
    // DASHBOARD
    // ===============================

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient dashboard")
    public ResponseEntity<PatientDashboardDto> getDashboard(@AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("Fetching dashboard for patient: {}", currentUser.getUsername());
        PatientDashboardDto dashboard = patientService.getPatientDashboard(currentUser.getId());
        return ResponseEntity.ok(dashboard);
    }
}

