package com.hospital_management.services.medical;


import com.hospital_management.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MedicalRecordService {
    Page<MedicalRecordDto> getPatientMedicalRecords(Long patientId, LocalDate startDate, LocalDate endDate, String recordType, Pageable pageable);
    MedicalRecordDto getPatientMedicalRecord(Long patientId, Long recordId);
    String generateDownloadUrl(Long patientId, Long recordId);
    MedicalRecordDto createMedicalRecord(CreateMedicalRecordDto recordDto);
    MedicalRecordDto updateMedicalRecord(Long recordId, UpdateMedicalRecordDto updateDto);
    void deleteMedicalRecord(Long recordId);
}

