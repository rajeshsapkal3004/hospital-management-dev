package com.hospital_management.mapper;


import com.hospital_management.dtos.AllergyDto;
import com.hospital_management.models.Allergy;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AllergyMapper {

    AllergyMapper INSTANCE = Mappers.getMapper(AllergyMapper.class);

    @Mapping(target = "requiresAttention", expression = "java(allergy.requiresAttention())")
    AllergyDto toDto(Allergy allergy);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    Allergy toEntity(AllergyDto dto);
}
