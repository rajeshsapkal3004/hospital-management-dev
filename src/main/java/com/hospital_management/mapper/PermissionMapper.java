package com.hospital_management.mapper;

import com.hospital_management.dtos.PermissionDto;
import com.hospital_management.models.Permission;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionDto toDto(Permission permission);

    Set<PermissionDto> toDtoSet(Set<Permission> permissions);

    List<PermissionDto> toDtoList(List<Permission> permissions);
}
