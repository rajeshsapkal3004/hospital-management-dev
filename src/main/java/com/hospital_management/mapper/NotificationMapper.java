package com.hospital_management.mapper;


import com.hospital_management.dtos.NotificationDto;
import com.hospital_management.models.Notification;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mapping(target = "isNew", expression = "java(notification.isNew())")
    NotificationDto toDto(Notification notification);

    @InheritInverseConfiguration
    Notification toEntity(NotificationDto dto);
}
