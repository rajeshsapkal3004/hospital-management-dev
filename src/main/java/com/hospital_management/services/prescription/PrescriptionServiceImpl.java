package com.hospital_management.services.prescription;


import com.hospital_management.dtos.CreatePrescriptionDto;
import com.hospital_management.dtos.CreatePrescriptionItemDto;
import com.hospital_management.dtos.PrescriptionDto;
import com.hospital_management.dtos.UpdatePrescriptionDto;
import com.hospital_management.exception.MedicalRecordNotFoundException;
import com.hospital_management.exception.PrescriptionNotFoundException;
import com.hospital_management.mapper.PrescriptionMapper;
import com.hospital_management.models.MedicalRecord;
import com.hospital_management.models.Prescription;
import com.hospital_management.models.PrescriptionItem;
import com.hospital_management.repo.MedicalRecordRepository;
import com.hospital_management.repo.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDto> getPatientPrescriptions(Long patientId, LocalDate startDate,
                                                         LocalDate endDate, String status, Pageable pageable) {
        log.debug("Fetching prescriptions for patient: {}", patientId);

        Specification<Prescription> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("prescribedDate")), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("prescribedDate")), endDate));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"),
                        Prescription.PrescriptionStatus.valueOf(status.toUpperCase())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Prescription> prescriptions = prescriptionRepository.findAll(spec, pageable);
        return prescriptions.map(prescriptionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionDto getPatientPrescription(Long patientId, Long prescriptionId) {
        log.debug("Fetching prescription {} for patient: {}", prescriptionId, patientId);

        Prescription prescription = prescriptionRepository.findByIdAndPatientId(prescriptionId, patientId)
                .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

        return prescriptionMapper.toDto(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionDto> getActivePrescriptions(Long patientId) {
        log.debug("Fetching active prescriptions for patient: {}", patientId);

        List<Prescription> prescriptions = prescriptionRepository.findActivePrescriptions(patientId);

        return prescriptions.stream()
                .map(prescriptionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PrescriptionDto createPrescription(CreatePrescriptionDto dto) {
        log.info("Creating prescription for patient: {}", dto.getPatientId());

        try {
            // Load related entities
            MedicalRecord medicalRecord = null;
            if (dto.getMedicalRecordId() != null) {
                medicalRecord = medicalRecordRepository.findById(dto.getMedicalRecordId())
                        .orElseThrow(() -> new MedicalRecordNotFoundException(dto.getMedicalRecordId()));
            }

            Prescription prescription = Prescription.builder()
                    .patientId(dto.getPatientId())
                    .doctorId(dto.getDoctorId())
                    .appointmentId(dto.getAppointmentId())
                    .validUntil(dto.getValidUntil())
                    .diagnosis(dto.getDiagnosis())
                    .patientInstructions(dto.getPatientInstructions())
                    .pharmacyNotes(dto.getPharmacyNotes())
                    .additionalNotes(dto.getAdditionalNotes())
                    .priority(dto.getPriority())
                    .isRepeatable(dto.getIsRepeatable())
                    .repeatCount(dto.getRepeatCount())
                    .status(Prescription.PrescriptionStatus.ACTIVE)
                    .prescribedDate(LocalDateTime.now())
                    .build();

            // Associate medicalRecord if provided
            if (medicalRecord != null) {
                prescription.setMedicalRecord(medicalRecord);
            }

            // Add prescription items
            for (CreatePrescriptionItemDto itemDto : dto.getPrescriptionItems()) {
                PrescriptionItem item = PrescriptionItem.builder()
                        .medicationName(itemDto.getMedicationName())
                        .genericName(itemDto.getGenericName())
                        .brandName(itemDto.getBrandName())
                        .strength(itemDto.getStrength())
                        .dosageForm(itemDto.getDosageForm())
                        .quantity(itemDto.getQuantity())
                        .unit(itemDto.getUnit())
                        .dosageInstructions(itemDto.getDosageInstructions())
                        .frequency(itemDto.getFrequency())
                        .duration(itemDto.getDuration())
                        .route(itemDto.getRoute())
                        .specialInstructions(itemDto.getSpecialInstructions())
                        .warnings(itemDto.getWarnings())
                        .substitutionAllowed(itemDto.getSubstitutionAllowed())
                        .unitPrice(itemDto.getUnitPrice())
                        .build();
                prescription.addPrescriptionItem(item);
            }

            Prescription saved = prescriptionRepository.save(prescription);
            return prescriptionMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error creating prescription", e);
            throw new RuntimeException("Failed to create prescription: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional
    public PrescriptionDto updatePrescription(Long prescriptionId, UpdatePrescriptionDto dto) {
        log.info("Updating prescription: {}", prescriptionId);

        try {
            Prescription prescription = prescriptionRepository.findById(prescriptionId)
                    .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

            prescription.setValidUntil(dto.getValidUntil());
            prescription.setDiagnosis(dto.getDiagnosis());
            prescription.setPatientInstructions(dto.getPatientInstructions());
            prescription.setPharmacyNotes(dto.getPharmacyNotes());
            prescription.setAdditionalNotes(dto.getAdditionalNotes());

            Prescription saved = prescriptionRepository.save(prescription);
            return prescriptionMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating prescription", e);
            throw new RuntimeException("Failed to update prescription: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void dispensePrescription(Long prescriptionId) {
        log.info("Dispensing prescription: {}", prescriptionId);

        try {
            Prescription prescription = prescriptionRepository.findById(prescriptionId)
                    .orElseThrow(() -> new PrescriptionNotFoundException(prescriptionId));

            prescription.setStatus(Prescription.PrescriptionStatus.DISPENSED);
            prescription.setDispensedDate(LocalDateTime.now());

            prescriptionRepository.save(prescription);

        } catch (Exception e) {
            log.error("Error dispensing prescription", e);
            throw new RuntimeException("Failed to dispense prescription: " + e.getMessage());
        }
    }
}

