package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "receptionists", uniqueConstraints = {
        @UniqueConstraint(columnNames = "employee_id")
})
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("RECEPTIONIST")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Receptionist extends User{

    @NotBlank(message = "Employee ID is required for receptionists")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Employee ID can only contain uppercase letters, numbers, and hyphens")
    @Column(name = "employee_id", unique = true, nullable = false, length = 20)
    private String employeeId;

    @NotBlank(message = "Department is required for receptionists")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", length = 20)
    private Shift shift;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "shift_start_time")
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time")
    private LocalTime shiftEndTime;

    @Size(max = 50, message = "Desk location cannot exceed 50 characters")
    @Column(name = "desk_location", length = 50)
    private String deskLocation;

    @Size(max = 100, message = "Supervisor name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Supervisor name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "supervisor_name", length = 100)
    private String supervisorName;

    @Builder.Default
    @Column(name = "can_handle_payments", nullable = false)
    private Boolean canHandlePayments = true;

    @Builder.Default
    @Column(name = "can_schedule_appointments", nullable = false)
    private Boolean canScheduleAppointments = true;

    @Builder.Default
    @Column(name = "can_access_medical_records", nullable = false)
    private Boolean canAccessMedicalRecords = false;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 40, message = "Years of experience cannot exceed 40")
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Size(max = 20, message = "Extension number cannot exceed 20 characters")
    @Pattern(regexp = "^[0-9-]+$", message = "Extension number can only contain numbers and hyphens")
    @Column(name = "extension_number", length = 20)
    private String extensionNumber;

    // Custom constructors
    public Receptionist(String username, String email, String password, String firstName, String lastName,
                        String employeeId, String department) {
        super(username, email, password, firstName, lastName);
        this.employeeId = employeeId;
        this.department = department;
        this.canHandlePayments = true;
        this.canScheduleAppointments = true;
        this.canAccessMedicalRecords = false;
    }

    // Business methods
    public boolean canWorkShift(Shift requestedShift) {
        return this.shift == null || this.shift == requestedShift;
    }

    public String getReceptionistCode() {
        return "REC-" + this.employeeId;
    }

    public boolean isCurrentlyOnDuty() {
        if (shiftStartTime == null || shiftEndTime == null) {
            return true; // No specific shift times set
        }

        LocalTime now = LocalTime.now();
        return !now.isBefore(shiftStartTime) && !now.isAfter(shiftEndTime);
    }

    public boolean canPerformTask(String task) {
        switch (task.toLowerCase()) {
            case "payment":
                return this.canHandlePayments;
            case "appointment":
                return this.canScheduleAppointments;
            case "medical_record":
                return this.canAccessMedicalRecords;
            default:
                return true;
        }
    }

    public enum Shift {
        MORNING("Morning Shift", "08:00-16:00"),
        EVENING("Evening Shift", "16:00-00:00"),
        NIGHT("Night Shift", "00:00-08:00"),
        DAY("Day Shift", "09:00-17:00"),
        ROTATING("Rotating Shift", "Variable");

        private final String displayName;
        private final String timeRange;

        Shift(String displayName, String timeRange) {
            this.displayName = displayName;
            this.timeRange = timeRange;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getTimeRange() {
            return timeRange;
        }
    }


}
