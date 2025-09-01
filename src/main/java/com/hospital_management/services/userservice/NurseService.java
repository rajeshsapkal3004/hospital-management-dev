package com.hospital_management.services.userservice;


import com.hospital_management.dtos.NurseDto;
import com.hospital_management.dtos.NurseRegistrationDto;
import com.hospital_management.models.Nurse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface NurseService {

    // Registration and basic operations
    NurseDto registerNurse(NurseRegistrationDto registrationDto);
    NurseDto getNurseById(Long id);
    NurseDto getNurseByLicenseNumber(String licenseNumber);
    NurseDto getNurseByEmployeeId(String employeeId);

    // Update operations
    NurseDto updateNurse(Long id, NurseRegistrationDto updateDto);
    void updateAvailabilityStatus(Long nurseId, boolean available);
    void updateShift(Long nurseId, Nurse.Shift shift);
    void updateWardAssignment(Long nurseId, String ward);
    void updateHeadNurseStatus(Long nurseId, boolean isHeadNurse);

    // Search and filtering
    Page<NurseDto> getAllNurses(Pageable pageable);
    List<NurseDto> getAvailableNurses();
    List<NurseDto> getNursesByDepartment(String department);
    List<NurseDto> getNursesByShift(Nurse.Shift shift);
    List<NurseDto> getNursesByWard(String ward);
    List<NurseDto> getHeadNurses();
    List<NurseDto> getNursesWithExpiringCertification(LocalDate date);
    Page<NurseDto> searchNurses(String department, Nurse.Shift shift, Nurse.NurseType nurseType,
                                String wardAssignment, Integer minExperience, Boolean available,
                                Boolean headNurse, Pageable pageable);

    // Specialization management
    void addSpecialization(Long nurseId, String specialization);
    void removeSpecialization(Long nurseId, String specialization);

    // Validation
    boolean existsByLicenseNumber(String licenseNumber);
    boolean existsByEmployeeId(String employeeId);

    // Statistics
    long getTotalNurseCount();
    long getAvailableNurseCount();
    long getHeadNurseCount();
    Map<String, Long> getNurseCountByDepartment();
    Map<String, Long> getNurseCountByShift();
    Map<String, Long> getNurseCountByType();
    Map<String, Long> getNurseCountByWard();
    Double getAverageExperience();
}

