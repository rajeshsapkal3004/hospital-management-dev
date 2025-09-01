package com.hospital_management.mapper;


import com.hospital_management.dtos.InsuranceDto;
import com.hospital_management.models.Insurance;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsuranceMapper {

    InsuranceMapper INSTANCE = Mappers.getMapper(InsuranceMapper.class);

    @Mapping(target = "isExpired", expression = "java(insurance.isExpired())")
    InsuranceDto toDto(Insurance insurance);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    Insurance toEntity(InsuranceDto dto);
}

