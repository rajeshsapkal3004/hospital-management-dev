package com.hospital_management.mapper;



import com.hospital_management.dtos.PrescriptionItemDto;
import com.hospital_management.models.PrescriptionItem;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PrescriptionItemMapper {

    PrescriptionItemMapper INSTANCE = Mappers.getMapper(PrescriptionItemMapper.class);

    @Mapping(target = "substitutionDisplayName", expression = "java(item.getSubstitutionAllowed().getDisplayName())")
    @Mapping(target = "isFullyDispensed", expression = "java(item.isFullyDispensed())")
    @Mapping(target = "isPartiallyDispensed", expression = "java(item.isPartiallyDispensed())")
    PrescriptionItemDto toDto(PrescriptionItem item);

    @InheritInverseConfiguration
    @Mapping(target = "prescription", ignore = true)
    PrescriptionItem toEntity(PrescriptionItemDto dto);
}
