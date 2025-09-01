package com.hospital_management.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_record_attachments")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"medicalRecord"})
@ToString(exclude = {"medicalRecord"})
public class MedicalRecordAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Size(max = 100, message = "File type cannot exceed 100 characters")
    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "attachment_type", nullable = false, length = 30)
    private AttachmentType attachmentType = AttachmentType.DOCUMENT;

    @CreatedDate
    @Column(name = "uploaded_date", updatable = false)
    private LocalDateTime uploadedDate;

    @Column(name = "uploaded_by", length = 50)
    private String uploadedBy;

    public enum AttachmentType {
        DOCUMENT("Document"),
        IMAGE("Image"),
        XRAY("X-Ray"),
        SCAN("Scan"),
        LAB_REPORT("Lab Report"),
        PRESCRIPTION("Prescription"),
        OTHER("Other");

        private final String displayName;

        AttachmentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
