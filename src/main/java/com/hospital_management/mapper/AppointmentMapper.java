package com.hospital_management.mapper;


import com.hospital_management.dtos.AppointmentDto;
import com.hospital_management.models.Appointment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(appointment.getPatient().getFirstName() + \" \" + appointment.getPatient().getLastName())")
    @Mapping(target = "patientEmail", source = "patient.email")
    @Mapping(target = "patientPhone", source = "patient.phone")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", expression = "java(appointment.getDoctor().getFirstName() + \" \" + appointment.getDoctor().getLastName())")
    @Mapping(target = "doctorSpecialization", source = "doctor.specialization")
    @Mapping(target = "statusDisplayName", expression = "java(appointment.getStatus() != null ? appointment.getStatus().getDisplayName() : null)")
    @Mapping(target = "priorityDisplayName", expression = "java(appointment.getPriority() != null ? appointment.getPriority().getDisplayName() : null)")
    @Mapping(target = "canBeRescheduled", expression = "java(appointment.canBeRescheduled())")
    @Mapping(target = "canBeCancelled", expression = "java(appointment.canBeCancelled())")
    @Mapping(target = "isUpcoming", expression = "java(appointment.isUpcoming())")
    @Mapping(target = "isPast", expression = "java(appointment.isPast())")
    AppointmentDto toDto(Appointment appointment);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Appointment toEntity(AppointmentDto dto);
}
