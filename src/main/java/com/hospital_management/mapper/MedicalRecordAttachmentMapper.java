package com.hospital_management.mapper;


import com.hospital_management.dtos.MedicalRecordAttachmentDto;
import com.hospital_management.models.MedicalRecordAttachment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicalRecordAttachmentMapper {

    MedicalRecordAttachmentMapper INSTANCE = Mappers.getMapper(MedicalRecordAttachmentMapper.class);

    @Mapping(target = "attachmentTypeDisplayName", expression = "java(attachment.getAttachmentType().getDisplayName())")
    @Mapping(target = "fileSizeFormatted", expression = "java(formatFileSize(attachment.getFileSize()))")
    MedicalRecordAttachmentDto toDto(MedicalRecordAttachment attachment);

    @InheritInverseConfiguration
    @Mapping(target = "medicalRecord", ignore = true)
    MedicalRecordAttachment toEntity(MedicalRecordAttachmentDto dto);

    default String formatFileSize(Long fileSize) {
        if (fileSize == null) return null;

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
