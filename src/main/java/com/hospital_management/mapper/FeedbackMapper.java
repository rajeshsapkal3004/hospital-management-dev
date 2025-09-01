package com.hospital_management.mapper;


import com.hospital_management.dtos.FeedbackDto;
import com.hospital_management.models.Feedback;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedbackMapper {

    FeedbackMapper INSTANCE = Mappers.getMapper(FeedbackMapper.class);

    FeedbackDto toDto(Feedback feedback);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    Feedback toEntity(FeedbackDto dto);
}
