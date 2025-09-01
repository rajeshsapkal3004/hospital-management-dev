package com.hospital_management.mapper;

import com.hospital_management.dtos.DepartmentDto;
import com.hospital_management.models.Department;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    DepartmentDto toDto(Department department);

    Department toEntity(DepartmentDto departmentDto);

    // If you don't have MapStruct, use this manual implementation:
}

