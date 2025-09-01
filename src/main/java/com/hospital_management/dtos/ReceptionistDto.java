package com.hospital_management.dtos;


import com.hospital_management.models.Receptionist;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReceptionistDto extends UserDto {
    private String employeeId;
    private String department;
    private Receptionist.Shift shift;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private String deskLocation;
    private String extensionNumber;
    private String supervisorName;
    private Integer yearsOfExperience;
    private LocalDate hireDate;
    private boolean canHandlePayments;
    private boolean canScheduleAppointments;
    private boolean canAccessMedicalRecords;
}
