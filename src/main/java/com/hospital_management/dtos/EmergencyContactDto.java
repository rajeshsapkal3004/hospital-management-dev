package com.hospital_management.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactDto {

    private Long id;
    private Long patientId;

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name cannot exceed 100 characters")
    private String contactName;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50, message = "Relationship cannot exceed 50 characters")
    private String relationship;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @Builder.Default
    private Boolean isPrimary = false;

    @Builder.Default
    private Boolean isActive = true;

    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
