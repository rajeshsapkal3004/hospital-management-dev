package com.hospital_management.dtos;

import com.hospital_management.models.MedicalRecordAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordAttachmentDto {

    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String description;
    private MedicalRecordAttachment.AttachmentType attachmentType;
    private LocalDateTime uploadedDate;
    private String uploadedBy;

    // Computed fields
    private String attachmentTypeDisplayName;
    private String fileSizeFormatted;
    private String downloadUrl;

    public String getAttachmentTypeDisplayName() {
        return attachmentType != null ? attachmentType.getDisplayName() : null;
    }

    public String getFileSizeFormatted() {
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
