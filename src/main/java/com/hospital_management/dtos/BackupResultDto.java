package com.hospital_management.dtos;

import com.hospital_management.enumclasses.BackupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackupResultDto {
    private String backupId;
    private String status; // SUCCESS, FAILED, IN_PROGRESS
    private String message;
    private String filePath;
    private Long fileSize; // in bytes
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Double compressionRatio;
    private String checksumMd5;
    private BackupType type;
    private String description;
}