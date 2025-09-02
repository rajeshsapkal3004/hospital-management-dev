package com.hospital_management.repo;



import com.hospital_management.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    // Find appointment by ID and patient ID for security
    Optional<Appointment> findByIdAndPatientId(Long id, Long patientId);

    // Find upcoming appointments for a patient
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findUpcomingAppointments(@Param("patientId") Long patientId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Find doctor appointments for a specific date range
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findDoctorAppointments(@Param("doctorId") Long doctorId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "AND a.appointmentDateTime >= :rangeStart " +
            "AND a.appointmentDateTime < :rangeEnd")
    long countConflictingAppointments(@Param("doctorId") Long doctorId,
                                      @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEnd") LocalDateTime rangeEnd);

    // Find appointments by status
    List<Appointment> findByStatusAndAppointmentDateTimeBetween(
            Appointment.AppointmentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Find patient appointments in date range
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findPatientAppointmentsInRange(@Param("patientId") Long patientId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

//    // Find today's appointments for a doctor
//    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
//            "AND DATE(a.appointmentDateTime) = CURRENT_DATE " +
//            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
//            "ORDER BY a.appointmentDateTime ASC")
//    List<Appointment> findTodayAppointmentsForDoctor(@Param("doctorId") Long doctorId);

    // Find overdue appointments (past scheduled time but not completed)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime < :currentTime " +
            "AND a.status = 'SCHEDULED' " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findOverdueAppointments(@Param("currentTime") LocalDateTime currentTime);

    // Find appointments needing follow-up
    @Query("SELECT a FROM Appointment a WHERE a.status = 'COMPLETED' " +
            "AND a.isFollowUp = false " +
            "AND a.completedAt < :cutoffDate " +
            "AND NOT EXISTS (SELECT f FROM Appointment f WHERE f.followUpFor = a.id)")
    List<Appointment> findAppointmentsNeedingFollowUp(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Count appointments by status for a date range
    @Query("SELECT a.status, COUNT(a) FROM Appointment a " +
            "WHERE a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    // Find appointments by reference number
    Optional<Appointment> findByReferenceNumber(String referenceNumber);

    // Find emergency appointments
    @Query("SELECT a FROM Appointment a WHERE a.isEmergency = true " +
            "AND a.appointmentDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findEmergencyAppointments(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Find no-show appointments
    @Query("SELECT a FROM Appointment a WHERE a.status = 'NO_SHOW' " +
            "AND a.patient.id = :patientId " +
            "ORDER BY a.appointmentDateTime DESC")
    List<Appointment> findPatientNoShowAppointments(@Param("patientId") Long patientId);

    // Custom method to find available slots (used in service logic)
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND DATE(a.appointmentDateTime) = DATE(:date) " +
            "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    List<Appointment> findDoctorAppointmentsOnDate(@Param("doctorId") Long doctorId,
                                                   @Param("date") LocalDateTime date);

    // Add these methods to AppointmentRepository interface

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId")
    Integer countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND a.status = 'COMPLETED'")
    Integer countCompletedByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
            "AND a.appointmentDateTime > CURRENT_TIMESTAMP")
    Integer countUpcomingByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND a.status = 'CANCELLED'")
    Integer countCancelledByPatientId(@Param("patientId") Long patientId);

}
