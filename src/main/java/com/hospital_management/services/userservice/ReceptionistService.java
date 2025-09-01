package com.hospital_management.services.userservice;


import com.hospital_management.dtos.ReceptionistDto;
import com.hospital_management.dtos.ReceptionistRegistrationDto;
import com.hospital_management.models.Receptionist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface ReceptionistService {

    // Registration and basic operations
    ReceptionistDto registerReceptionist(ReceptionistRegistrationDto registrationDto);
    ReceptionistDto getReceptionistById(Long id);
    ReceptionistDto getReceptionistByEmployeeId(String employeeId);
    ReceptionistDto getReceptionistByExtensionNumber(String extensionNumber);

    // Update operations
    ReceptionistDto updateReceptionist(Long id, ReceptionistRegistrationDto updateDto);
    void updateShift(Long receptionistId, Receptionist.Shift shift);
    void updateShiftTimes(Long receptionistId, LocalTime startTime, LocalTime endTime);
    void updateDeskLocation(Long receptionistId, String deskLocation);
    void updateExtensionNumber(Long receptionistId, String extensionNumber);

    // Permission management
    void updatePaymentHandlingPermission(Long receptionistId, boolean canHandle);
    void updateAppointmentSchedulingPermission(Long receptionistId, boolean canSchedule);
    void updateMedicalRecordAccessPermission(Long receptionistId, boolean canAccess);

    // Search and filtering
    Page<ReceptionistDto> getAllReceptionists(Pageable pageable);
    List<ReceptionistDto> getAllActiveReceptionists();
    List<ReceptionistDto> getReceptionistsByDepartment(String department);
    List<ReceptionistDto> getReceptionistsByShift(Receptionist.Shift shift);
    List<ReceptionistDto> getReceptionistsByDeskLocation(String deskLocation);
    List<ReceptionistDto> getReceptionistsCurrentlyOnDuty(LocalTime currentTime);
    List<ReceptionistDto> getReceptionistsWhoCanHandlePayments();
    List<ReceptionistDto> getReceptionistsWhoCanScheduleAppointments();
    List<ReceptionistDto> getReceptionistsWhoCanAccessMedicalRecords();
    List<ReceptionistDto> getFullServiceReceptionists();
    Page<ReceptionistDto> searchReceptionists(String department, Receptionist.Shift shift,
                                              String deskLocation, Integer minExperience,
                                              Boolean canHandlePayments, Boolean canScheduleAppointments,
                                              Pageable pageable);

    // Validation
    boolean existsByEmployeeId(String employeeId);
    boolean existsByExtensionNumber(String extensionNumber);
    boolean isEmployeeIdAvailable(String employeeId, Long excludeReceptionistId);
    boolean isExtensionNumberAvailable(String extensionNumber, Long excludeReceptionistId);

    // Statistics
    long getTotalReceptionistCount();
    long getActiveReceptionistCount();
    Map<String, Long> getReceptionistCountByDepartment();
    Map<String, Long> getReceptionistCountByShift();
    Map<String, Long> getReceptionistCountByDeskLocation();
    Double getAverageExperience();
    Map<String, Long> getReceptionistCoverageByDepartmentAndShift();
}

