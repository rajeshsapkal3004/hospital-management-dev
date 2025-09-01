package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageConfigDto {
    private String uploadPath;
    private Long maxFileSize; // in bytes
    private String[] allowedFileTypes;
    private Boolean enableVirusScanning;
    private Integer retentionDays;
    private String storageType; // LOCAL, AWS_S3, AZURE_BLOB
}