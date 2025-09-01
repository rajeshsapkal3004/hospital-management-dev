package com.hospital_management.mapper;


import com.hospital_management.dtos.DischargeSummaryDto;
import com.hospital_management.models.DischargeSummary;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DischargeSummaryMapper {

    DischargeSummaryMapper INSTANCE = Mappers.getMapper(DischargeSummaryMapper.class);

    @Mapping(target = "medicalRecordId", source = "medicalRecord.id")
    DischargeSummaryDto toDto(DischargeSummary dischargeSummary);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    DischargeSummary toEntity(DischargeSummaryDto dto);
}
