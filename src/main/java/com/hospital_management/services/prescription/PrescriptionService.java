package com.hospital_management.services.prescription;



import com.hospital_management.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionService {
    Page<PrescriptionDto> getPatientPrescriptions(Long patientId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable);
    PrescriptionDto getPatientPrescription(Long patientId, Long prescriptionId);
    List<PrescriptionDto> getActivePrescriptions(Long patientId);
    PrescriptionDto createPrescription(CreatePrescriptionDto prescriptionDto);
    PrescriptionDto updatePrescription(Long prescriptionId, UpdatePrescriptionDto updateDto);
    void dispensePrescription(Long prescriptionId);
}

