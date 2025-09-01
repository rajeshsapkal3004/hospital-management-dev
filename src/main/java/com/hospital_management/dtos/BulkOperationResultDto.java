package com.hospital_management.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkOperationResultDto {
    private int successCount;
    private int failureCount;
    private List<String> errors;
    private String message;
}

