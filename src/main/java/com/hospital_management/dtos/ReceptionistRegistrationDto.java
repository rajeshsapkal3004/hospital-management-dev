package com.hospital_management.dtos;
import com.hospital_management.models.Receptionist;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReceptionistRegistrationDto extends UserRegistrationDto {

    @NotBlank(message = "Employee ID is required for receptionists")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    private String employeeId;

    @NotBlank(message = "Department is required for receptionists")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    private String department;

    @NotNull(message = "Shift is required")
    private Receptionist.Shift shift;

    @NotNull(message = "Shift start time is required")
    private LocalTime shiftStartTime;

    @NotNull(message = "Shift end time is required")
    private LocalTime shiftEndTime;

    @Size(max = 50, message = "Desk location cannot exceed 50 characters")
    private String deskLocation;

    @Pattern(regexp = "^[0-9]{3,6}$", message = "Extension number must be 3-6 digits")
    private String extensionNumber;

    @Size(max = 100, message = "Supervisor name cannot exceed 100 characters")
    private String supervisorName;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    private Integer yearsOfExperience;

    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    private Boolean canHandlePayments = false;
    private Boolean canScheduleAppointments = true;
    private Boolean canAccessMedicalRecords = false;
}

