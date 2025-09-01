package com.hospital_management.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkUserActionDto {
    @NotEmpty
    private List<Long> userIds;

    @NotBlank
    private String action; // ACTIVATE, DEACTIVATE, LOCK, UNLOCK, DELETE

    private String reason;
}