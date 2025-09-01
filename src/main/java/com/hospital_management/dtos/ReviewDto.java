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
public class ReviewDto {

    private Long id;
    private Long patientId;
    private Long appointmentId;
    private Long doctorId;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 1000, message = "Review cannot exceed 1000 characters")
    private String reviewText;

    @Size(max = 500, message = "Recommendations cannot exceed 500 characters")
    private String recommendations;

    private Boolean wouldRecommend;
    private LocalDateTime submittedDate;
    private Boolean isVerified;
    private Boolean isPublic;
}
