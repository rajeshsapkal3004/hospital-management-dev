package com.hospital_management.services.appointment;

import com.hospital_management.dtos.AppointmentDto;
import com.hospital_management.dtos.AppointmentSlotDto;
import com.hospital_management.dtos.BookAppointmentDto;
import com.hospital_management.dtos.RescheduleAppointmentDto;
import com.hospital_management.exception.AppointmentNotFoundException;
import com.hospital_management.exception.InvalidAppointmentException;
import com.hospital_management.mapper.AppointmentMapper;
import com.hospital_management.models.Appointment;
import com.hospital_management.models.Doctor;
import com.hospital_management.models.Patient;
import com.hospital_management.repo.AppointmentRepository;
import com.hospital_management.repo.PatientRepository;
import com.hospital_management.repo.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentDto bookAppointment(BookAppointmentDto appointmentDto) {
        log.info("Booking appointment for patient: {}", appointmentDto.getPatientId());

        try {
            // Validate patient exists
            Patient patient = patientRepository.findById(appointmentDto.getPatientId())
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

            // Validate doctor exists
            Doctor doctor = doctorRepository.findById(appointmentDto.getDoctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

            // Check if slot is available
            if (!isSlotAvailable(appointmentDto.getDoctorId(), appointmentDto.getAppointmentDateTime())) {
                throw new InvalidAppointmentException("Selected time slot is not available");
            }

            // Create appointment
            Appointment appointment = Appointment.builder()
                    .patient(patient)
                    .doctor(doctor)
                    .appointmentDateTime(appointmentDto.getAppointmentDateTime())
                    .appointmentType(appointmentDto.getAppointmentType())
                    .reason(appointmentDto.getReason())
                    .notes(appointmentDto.getNotes())
                    .status(Appointment.AppointmentStatus.SCHEDULED)
                    .duration(appointmentDto.getDuration() != null ? appointmentDto.getDuration() : 30)
                    .build();

            Appointment saved = appointmentRepository.save(appointment);
            log.info("Appointment booked successfully: {}", saved.getId());

            return appointmentMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error booking appointment", e);
            throw new RuntimeException("Failed to book appointment: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentDto> getPatientAppointments(Long patientId, LocalDate startDate,
                                                       LocalDate endDate, String status, Pageable pageable) {
        log.debug("Fetching appointments for patient: {}", patientId);

        Specification<Appointment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("appointmentDateTime"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("appointmentDateTime"), endDate.atTime(23, 59, 59)));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"),
                        Appointment.AppointmentStatus.valueOf(status.toUpperCase())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Appointment> appointments = appointmentRepository.findAll(spec, pageable);
        return appointments.map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto getPatientAppointment(Long patientId, Long appointmentId) {
        log.debug("Fetching appointment {} for patient: {}", appointmentId, patientId);

        Appointment appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        return appointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional
    public AppointmentDto rescheduleAppointment(Long appointmentId, Long patientId,
                                                RescheduleAppointmentDto rescheduleDto) {
        log.info("Rescheduling appointment {} for patient: {}", appointmentId, patientId);

        try {
            Appointment appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                    .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

            // Check if appointment can be rescheduled
            if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED ||
                    appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
                throw new InvalidAppointmentException("Cannot reschedule completed or cancelled appointment");
            }

            // Check if new slot is available
            if (!isSlotAvailable(appointment.getDoctor().getId(), rescheduleDto.getNewDateTime())) {
                throw new InvalidAppointmentException("New time slot is not available");
            }

            appointment.setAppointmentDateTime(rescheduleDto.getNewDateTime());
            appointment.setRescheduleReason(rescheduleDto.getReason());
            appointment.setStatus(Appointment.AppointmentStatus.RESCHEDULED);

            Appointment saved = appointmentRepository.save(appointment);
            log.info("Appointment rescheduled successfully: {}", saved.getId());

            return appointmentMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error rescheduling appointment", e);
            throw new RuntimeException("Failed to reschedule appointment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long patientId, String reason) {
        log.info("Cancelling appointment {} for patient: {}", appointmentId, patientId);

        try {
            Appointment appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                    .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

            if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                throw new InvalidAppointmentException("Cannot cancel completed appointment");
            }

            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointment.setCancellationReason(reason);
            appointment.setCancelledAt(LocalDateTime.now());

            appointmentRepository.save(appointment);
            log.info("Appointment cancelled successfully: {}", appointmentId);

        } catch (Exception e) {
            log.error("Error cancelling appointment", e);
            throw new RuntimeException("Failed to cancel appointment: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointments(Long patientId, int days) {
        log.debug("Fetching upcoming appointments for patient: {}", patientId);

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);

        List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(
                patientId, startDate, endDate);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(Long doctorId, LocalDate date) {
        log.debug("Fetching appointments for doctor {} on date: {}", doctorId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Appointment> appointments = appointmentRepository.findDoctorAppointments(
                doctorId, startOfDay, endOfDay);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentDto updateAppointmentStatus(Long appointmentId, String status) {
        log.info("Updating appointment {} status to: {}", appointmentId, status);

        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

            appointment.setStatus(Appointment.AppointmentStatus.valueOf(status.toUpperCase()));

            if (status.equalsIgnoreCase("COMPLETED")) {
                appointment.setCompletedAt(LocalDateTime.now());
            }

            Appointment saved = appointmentRepository.save(appointment);
            return appointmentMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating appointment status", e);
            throw new RuntimeException("Failed to update appointment status: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        log.debug("Fetching available slots for doctor {} on date: {}", doctorId, date);

        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

            List<AppointmentSlotDto> availableSlots = new ArrayList<>();

            // Generate time slots (example: 9 AM to 5 PM, 30-minute slots)
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(17, 0);

            while (startTime.isBefore(endTime)) {
                LocalDateTime slotDateTime = date.atTime(startTime);

                if (isSlotAvailable(doctorId, slotDateTime)) {
                    availableSlots.add(AppointmentSlotDto.builder()
                            .dateTime(slotDateTime)
                            .available(true)
                            .duration(30)
                            .build());
                }

                startTime = startTime.plusMinutes(30);
            }

            return availableSlots;

        } catch (Exception e) {
            log.error("Error fetching available slots", e);
            throw new RuntimeException("Failed to fetch available slots: " + e.getMessage());
        }
    }

    private boolean isSlotAvailable(Long doctorId, LocalDateTime dateTime) {
        // Check for appointments in a 1-hour window around the requested time
        LocalDateTime rangeStart = dateTime.minusMinutes(30);
        LocalDateTime rangeEnd = dateTime.plusMinutes(30);

        long conflictingAppointments = appointmentRepository.countConflictingAppointments(
                doctorId, rangeStart, rangeEnd);

        return conflictingAppointments == 0;
    }

}

