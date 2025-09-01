package com.hospital_management.services.medical;


import com.hospital_management.dtos.CreateMedicalRecordDto;
import com.hospital_management.dtos.MedicalRecordDto;
import com.hospital_management.dtos.UpdateMedicalRecordDto;
import com.hospital_management.exception.MedicalRecordNotFoundException;
import com.hospital_management.mapper.MedicalRecordMapper;
import com.hospital_management.models.MedicalRecord;
import com.hospital_management.repo.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordDto> getPatientMedicalRecords(Long patientId, LocalDate startDate,
                                                           LocalDate endDate, String recordType, Pageable pageable) {
        log.debug("Fetching medical records for patient: {}", patientId);

        Specification<MedicalRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("recordDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("recordDate"), endDate));
            }

            if (recordType != null && !recordType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("recordType"), recordType));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<MedicalRecord> records = medicalRecordRepository.findAll(spec, pageable);
        return records.map(medicalRecordMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDto getPatientMedicalRecord(Long patientId, Long recordId) {
        log.debug("Fetching medical record {} for patient: {}", recordId, patientId);

        MedicalRecord record = medicalRecordRepository.findByIdAndPatientId(recordId, patientId)
                .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));

        return medicalRecordMapper.toDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateDownloadUrl(Long patientId, Long recordId) {
        log.info("Generating download URL for medical record {} of patient: {}", recordId, patientId);

        try {
            MedicalRecord record = medicalRecordRepository.findByIdAndPatientId(recordId, patientId)
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));

            // Generate a secure download token
            String downloadToken = UUID.randomUUID().toString();

            // In real implementation, you would:
            // 1. Store the token with expiration time
            // 2. Return a secure download URL
            String downloadUrl = "/api/medical-records/download/" + downloadToken;

            return downloadUrl;

        } catch (Exception e) {
            log.error("Error generating download URL", e);
            throw new RuntimeException("Failed to generate download URL: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public MedicalRecordDto createMedicalRecord(CreateMedicalRecordDto recordDto) {
        log.info("Creating medical record for patient: {}", recordDto.getPatientId());

        try {
            MedicalRecord record = MedicalRecord.builder()
                    .patientId(recordDto.getPatientId())
                    .doctorId(recordDto.getDoctorId())
                    .appointmentId(recordDto.getAppointmentId())
                    .recordDate(recordDto.getRecordDate())
                    .visitDateTime(recordDto.getVisitDateTime())
                    .recordType(recordDto.getRecordType())
                    .chiefComplaint(recordDto.getChiefComplaint())
                    .historyOfPresentIllness(recordDto.getHistoryOfPresentIllness())
                    .pastMedicalHistory(recordDto.getPastMedicalHistory())
                    .familyHistory(recordDto.getFamilyHistory())
                    .socialHistory(recordDto.getSocialHistory())
                    .allergies(recordDto.getAllergies())
                    .currentMedications(recordDto.getCurrentMedications())
                    .physicalExamination(recordDto.getPhysicalExamination())
                    .clinicalFindings(recordDto.getClinicalFindings())
                    .diagnosis(recordDto.getDiagnosis())
                    .icdCodes(recordDto.getIcdCodes())
                    .treatmentPlan(recordDto.getTreatmentPlan())
                    .proceduresPerformed(recordDto.getProceduresPerformed())
                    .labTestsOrdered(recordDto.getLabTestsOrdered())
                    .imagingOrdered(recordDto.getImagingOrdered())
                    .followUpInstructions(recordDto.getFollowUpInstructions())
                    .followUpDate(recordDto.getFollowUpDate())
                    .additionalNotes(recordDto.getAdditionalNotes())
                    .confidentialityLevel(recordDto.getConfidentialityLevel())
                    .temperature(recordDto.getTemperature())
                    .bloodPressureSystolic(recordDto.getBloodPressureSystolic())
                    .bloodPressureDiastolic(recordDto.getBloodPressureDiastolic())
                    .heartRate(recordDto.getHeartRate())
                    .respiratoryRate(recordDto.getRespiratoryRate())
                    .oxygenSaturation(recordDto.getOxygenSaturation())
                    .weight(recordDto.getWeight())
                    .height(recordDto.getHeight())
                    .isEmergency(recordDto.getIsEmergency())
                    .isFollowUp(recordDto.getIsFollowUp())
                    .previousRecordId(recordDto.getPreviousRecordId())
                    .recordStatus(MedicalRecord.RecordStatus.DRAFT)
                    .build();

            MedicalRecord saved = medicalRecordRepository.save(record);
            return medicalRecordMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error creating medical record", e);
            throw new RuntimeException("Failed to create medical record: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public MedicalRecordDto updateMedicalRecord(Long recordId, UpdateMedicalRecordDto updateDto) {
        log.info("Updating medical record: {}", recordId);

        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));

            // Update only fields present in UpdateMedicalRecordDto
            record.setChiefComplaint(updateDto.getChiefComplaint());
            record.setHistoryOfPresentIllness(updateDto.getHistoryOfPresentIllness());
            record.setPastMedicalHistory(updateDto.getPastMedicalHistory());
            record.setFamilyHistory(updateDto.getFamilyHistory());
            record.setSocialHistory(updateDto.getSocialHistory());
            record.setAllergies(updateDto.getAllergies());
            record.setCurrentMedications(updateDto.getCurrentMedications());
            record.setPhysicalExamination(updateDto.getPhysicalExamination());
            record.setClinicalFindings(updateDto.getClinicalFindings());
            record.setDiagnosis(updateDto.getDiagnosis());
            record.setIcdCodes(updateDto.getIcdCodes());
            record.setTreatmentPlan(updateDto.getTreatmentPlan());
            record.setProceduresPerformed(updateDto.getProceduresPerformed());
            record.setLabTestsOrdered(updateDto.getLabTestsOrdered());
            record.setImagingOrdered(updateDto.getImagingOrdered());
            record.setFollowUpInstructions(updateDto.getFollowUpInstructions());
            record.setFollowUpDate(updateDto.getFollowUpDate());
            record.setAdditionalNotes(updateDto.getAdditionalNotes());
            record.setDischargeSummary(updateDto.getDischargeSummary());

            // Update vital signs if provided
            record.setTemperature(updateDto.getTemperature());
            record.setBloodPressureSystolic(updateDto.getBloodPressureSystolic());
            record.setBloodPressureDiastolic(updateDto.getBloodPressureDiastolic());
            record.setHeartRate(updateDto.getHeartRate());
            record.setRespiratoryRate(updateDto.getRespiratoryRate());
            record.setOxygenSaturation(updateDto.getOxygenSaturation());
            record.setWeight(updateDto.getWeight());
            record.setHeight(updateDto.getHeight());

            // Recalculate BMI after updating vitals
            record.calculateBMI();

            MedicalRecord saved = medicalRecordRepository.save(record);
            return medicalRecordMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating medical record", e);
            throw new RuntimeException("Failed to update medical record: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void deleteMedicalRecord(Long recordId) {
        log.warn("Deleting medical record: {}", recordId);

        try {
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));

            medicalRecordRepository.delete(record);

        } catch (Exception e) {
            log.error("Error deleting medical record", e);
            throw new RuntimeException("Failed to delete medical record: " + e.getMessage());
        }
    }
}

