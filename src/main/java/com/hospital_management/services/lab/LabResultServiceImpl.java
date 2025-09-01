package com.hospital_management.services.lab;


import com.hospital_management.dtos.*;
import com.hospital_management.exception.LabResultNotFoundException;
import com.hospital_management.mapper.LabResultMapper;
import com.hospital_management.mapper.LabTestMapper;
import com.hospital_management.models.LabResult;
import com.hospital_management.models.LabTest;
import com.hospital_management.repo.LabResultRepository;
import com.hospital_management.repo.LabTestRepository;
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
public class LabResultServiceImpl implements LabResultService {

    private final LabResultRepository labResultRepository;
    private final LabTestRepository labTestRepository;
    private final LabResultMapper labResultMapper;
    private final LabTestMapper labTestMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<LabResultDto> getPatientLabResults(Long patientId, LocalDate startDate,
                                                   LocalDate endDate, String testType, Pageable pageable) {
        log.debug("Fetching lab results for patient: {}", patientId);

        Specification<LabResult> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("testDate")), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("testDate")), endDate));
            }

            if (testType != null && !testType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("testType"), testType));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<LabResult> results = labResultRepository.findAll(spec, pageable);
        return results.map(labResultMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public LabResultDto getPatientLabResult(Long patientId, Long resultId) {
        log.debug("Fetching lab result {} for patient: {}", resultId, patientId);

        LabResult result = labResultRepository.findByIdAndPatientId(resultId, patientId)
                .orElseThrow(() -> new LabResultNotFoundException(resultId));

        return labResultMapper.toDto(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestDto> getPendingLabTests(Long patientId) {
        log.debug("Fetching pending lab tests for patient: {}", patientId);

        List<LabTest> tests = labTestRepository.findPendingTests(patientId);

        return tests.stream()
                .map(labTestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LabResultDto createLabResult(CreateLabResultDto resultDto) {
        log.info("Creating lab result for patient: {}", resultDto.getPatientId());

        try {
            LabResult result = LabResult.builder()
                    .patientId(resultDto.getPatientId())
                    .testType(resultDto.getTestType())
                    .testDate(resultDto.getTestDate())
                    .results(resultDto.getResults())
                    .normalRange(resultDto.getNormalRange())
                    .status(resultDto.getStatus())
                    .notes(resultDto.getNotes())
                    .technicianId(resultDto.getTechnicianId())
                    .build();

            LabResult saved = labResultRepository.save(result);
            return labResultMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error creating lab result", e);
            throw new RuntimeException("Failed to create lab result: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public LabResultDto updateLabResult(Long resultId, UpdateLabResultDto updateDto) {
        log.info("Updating lab result: {}", resultId);

        try {
            LabResult result = labResultRepository.findById(resultId)
                    .orElseThrow(() -> new LabResultNotFoundException(resultId));

            result.setResults(updateDto.getResults());
            result.setNormalRange(updateDto.getNormalRange());
            result.setStatus(updateDto.getStatus());
            result.setNotes(updateDto.getNotes());

            LabResult saved = labResultRepository.save(result);
            return labResultMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating lab result", e);
            throw new RuntimeException("Failed to update lab result: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public LabTestDto orderLabTest(OrderLabTestDto testDto) {
        log.info("Ordering lab test for patient: {}", testDto.getPatientId());

        try {
            LabTest test = LabTest.builder()
                    .patientId(testDto.getPatientId())
                    .doctorId(testDto.getDoctorId())
                    .testType(testDto.getTestType())
                    .orderDate(LocalDateTime.now())
                    .status(LabTest.TestStatus.ORDERED)
                    .instructions(testDto.getInstructions())
                    .urgency(testDto.getUrgency())
                    .build();

            LabTest saved = labTestRepository.save(test);
            return labTestMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error ordering lab test", e);
            throw new RuntimeException("Failed to order lab test: " + e.getMessage());
        }
    }
}

