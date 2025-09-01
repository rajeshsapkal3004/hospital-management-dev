package com.hospital_management.dtos;

import com.hospital_management.models.LabTechnician;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class LabTechnicianDto extends UserDto {
    private String employeeId;
    private String certificationNumber;
    private String department;
    private String labSection;
    private LabTechnician.Shift shift;
    private LocalDate certificationExpiry;
    private LocalDate hireDate;
    private String supervisorName;
    private Integer yearsOfExperience;
    private boolean canApproveResults;
    private boolean canOperateEquipment;
    private boolean isSeniorTechnician;
    private String qualifications;
    private Set<String> specializations;
    private Set<String> certifiedEquipment;
    private boolean isCertificationExpiring;
}
