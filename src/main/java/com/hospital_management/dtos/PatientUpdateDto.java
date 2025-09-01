package com.hospital_management.dtos;


import com.hospital_management.models.Patient;
import com.hospital_management.models.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateDto {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, apostrophes, and hyphens")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, apostrophes, and hyphens")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number (10-15 digits)")
    private String phone;

    private User.Gender gender;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Pattern(regexp = "^[0-9]{5,10}$", message = "Please provide a valid postal code (5-10 digits)")
    private String postalCode;

    @Size(max = 50, message = "City cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "City can only contain letters, spaces, apostrophes, and hyphens")
    private String city;

    @Size(max = 50, message = "State cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "State can only contain letters, spaces, apostrophes, and hyphens")
    private String state;

    @Size(max = 50, message = "Country cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Country can only contain letters, spaces, apostrophes, and hyphens")
    private String country;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Blood group cannot exceed 20 characters")
    private String bloodGroup;

    @DecimalMin(value = "1.0", message = "Height must be at least 1 cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250 cm")
    private Double height; // in cm

    @DecimalMin(value = "1.0", message = "Weight must be at least 1 kg")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
    private Double weight; // in kg

    @Size(max = 100, message = "Occupation cannot exceed 100 characters")
    private String occupation;

    @Size(max = 200, message = "Employer cannot exceed 200 characters")
    private String employer;

    @Size(max = 2000, message = "Medical history cannot exceed 2000 characters")
    private String medicalHistory;

    @Size(max = 1000, message = "Family history cannot exceed 1000 characters")
    private String familyHistory;

    @Size(max = 500, message = "Social history cannot exceed 500 characters")
    private String socialHistory;

    private Patient.MaritalStatus maritalStatus;

    @Size(max = 100, message = "Preferred language cannot exceed 100 characters")
    private String preferredLanguage;

    @Size(max = 100, message = "Religion cannot exceed 100 characters")
    private String religion;

    private Boolean smokingStatus;
    private Boolean drinkingStatus;
}
