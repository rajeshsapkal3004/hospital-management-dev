package com.hospital_management.mapper;


import com.hospital_management.dtos.ReviewDto;
import com.hospital_management.models.Review;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

    ReviewDto toDto(Review review);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Review toEntity(ReviewDto dto);
}

