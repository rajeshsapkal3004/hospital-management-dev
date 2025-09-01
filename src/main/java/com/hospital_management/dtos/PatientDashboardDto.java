package com.hospital_management.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDashboardDto {

    private PatientDto patient;
    private List<AppointmentDto> upcomingAppointments;
    private List<PrescriptionDto> activePrescriptions;
    private List<LabResultDto> recentLabResults;
    private List<NotificationDto> recentNotifications;
    private VitalSignsDto latestVitalSigns;
    private DashboardStatsDto stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsDto {
        private Integer totalAppointments;
        private Integer completedAppointments;
        private Integer activePrescriptions;
        private Integer pendingLabTests;
        private Integer unreadNotifications;
    }
}
