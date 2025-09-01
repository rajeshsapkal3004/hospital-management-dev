package com.hospital_management.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

// AuditLogExportDto.java
@Data
public class AuditLogExportDto {
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private String format; // CSV, EXCEL, PDF
    private List<String> actions;
    private List<String> usernames;
    private String severity;
    private Boolean includeSystemEvents;
}
