package com.hospital_management.services.appointment;


import com.hospital_management.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentDto bookAppointment(BookAppointmentDto appointmentDto);
    Page<AppointmentDto> getPatientAppointments(Long patientId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable);
    AppointmentDto getPatientAppointment(Long patientId, Long appointmentId);
    AppointmentDto rescheduleAppointment(Long appointmentId, Long patientId, RescheduleAppointmentDto rescheduleDto);
    void cancelAppointment(Long appointmentId, Long patientId, String reason);
    List<AppointmentDto> getUpcomingAppointments(Long patientId, int days);
    List<AppointmentDto> getDoctorAppointments(Long doctorId, LocalDate date);
    AppointmentDto updateAppointmentStatus(Long appointmentId, String status);
    List<AppointmentSlotDto> getAvailableSlots(Long doctorId, LocalDate date);
}

