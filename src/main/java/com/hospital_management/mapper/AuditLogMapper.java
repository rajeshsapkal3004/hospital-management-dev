package com.hospital_management.mapper;


import com.hospital_management.dtos.AuditLogDto;
import com.hospital_management.models.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogDto toDto(AuditLog auditLog);

    List<AuditLogDto> toDtoList(List<AuditLog> auditLogs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    AuditLog toEntity(AuditLogDto auditLogDto);
}

