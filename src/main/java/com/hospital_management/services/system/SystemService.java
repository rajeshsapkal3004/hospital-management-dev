package com.hospital_management.services.system;

import com.hospital_management.dtos.*;

import java.util.List;

public interface SystemService {
    SystemHealthDto getSystemHealth();
    SystemPerformanceDto getPerformanceMetrics();
    void setMaintenanceMode(boolean enable, String reason);
    BackupResultDto createBackup(String description);
    List<BackupInfoDto> getAvailableBackups();
    void restoreFromBackup(String backupId);
    SystemConfigDto getSystemConfiguration();
    SystemConfigDto updateSystemConfiguration(SystemConfigDto configDto);
}

