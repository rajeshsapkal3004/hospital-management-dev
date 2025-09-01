package com.hospital_management.services.lab;


import com.hospital_management.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LabResultService {
    Page<LabResultDto> getPatientLabResults(Long patientId, LocalDate startDate, LocalDate endDate, String testType, Pageable pageable);
    LabResultDto getPatientLabResult(Long patientId, Long resultId);
    List<LabTestDto> getPendingLabTests(Long patientId);
    LabResultDto createLabResult(CreateLabResultDto resultDto);
    LabResultDto updateLabResult(Long resultId, UpdateLabResultDto updateDto);
    LabTestDto orderLabTest(OrderLabTestDto testDto);
}

