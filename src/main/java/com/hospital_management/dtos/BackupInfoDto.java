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
public class BackupInfoDto {
    private String backupId;
    private String filename;
    private Long size; // in bytes
    private LocalDateTime createdAt;
    private String description;
    private BackupType type;
    private String status;
    private String createdBy;
    private Boolean canRestore;
    private String location;
}