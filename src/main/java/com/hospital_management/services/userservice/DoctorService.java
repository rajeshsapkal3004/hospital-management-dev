package com.hospital_management.services.userservice;

import com.hospital_management.dtos.DoctorDto;
import com.hospital_management.dtos.DoctorRegistrationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DoctorService {

    // Registration and basic operations
    DoctorDto registerDoctor(DoctorRegistrationDto registrationDto);
    DoctorDto getDoctorById(Long id);
    DoctorDto getDoctorByLicenseNumber(String licenseNumber);
    DoctorDto getDoctorByEmployeeId(String employeeId);

    // Update operations
    DoctorDto updateDoctor(Long id, DoctorRegistrationDto updateDto);
    void updateAvailabilityStatus(Long doctorId, boolean available);
    void updateConsultationFee(Long doctorId, BigDecimal fee);
    void updateRoomNumber(Long doctorId, String roomNumber);

    // Search and filtering
    Page<DoctorDto> getAllDoctors(Pageable pageable);
    List<DoctorDto> getAvailableDoctors();
    List<DoctorDto> getDoctorsBySpecialization(String specialization);
    List<DoctorDto> getDoctorsByDepartment(String department);
    List<DoctorDto> getDoctorsWithExpiringLicense(LocalDate date);
    Page<DoctorDto> searchDoctors(String specialization, String department,
                                  Integer minExperience, BigDecimal maxFee,
                                  Boolean available, Pageable pageable);

    // Certification management
    void addCertification(Long doctorId, String certification);
    void removeCertification(Long doctorId, String certification);

    // Emergency contact management
    void setEmergencyContact(Long doctorId, boolean isEmergencyContact);
    List<DoctorDto> getEmergencyContactDoctors();

    // Validation
    boolean existsByLicenseNumber(String licenseNumber);
    boolean existsByEmployeeId(String employeeId);
    boolean isLicenseNumberAvailable(String licenseNumber, Long excludeDoctorId);

    // Statistics
    long getTotalDoctorCount();
    long getAvailableDoctorCount();
    Map<String, Long> getDoctorCountBySpecialization();
    Map<String, Long> getDoctorCountByDepartment();
    BigDecimal getAverageConsultationFee();
}
