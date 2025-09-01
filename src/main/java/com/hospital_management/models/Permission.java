package com.hospital_management.models;
import com.hospital_management.models.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resource", "action"}),
        @UniqueConstraint(columnNames = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"roles"})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Permission name is required")
    @Size(min = 3, max = 100, message = "Permission name must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Permission name must contain only uppercase letters and underscores")
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @NotBlank(message = "Resource is required")
    @Size(min = 2, max = 50, message = "Resource name must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Resource name must contain only uppercase letters and underscores")
    @Column(name = "resource", nullable = false, length = 50)
    private String resource;

    @NotBlank(message = "Action is required")
    @Size(min = 2, max = 20, message = "Action name must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Action name must contain only uppercase letters and underscores")
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_system_permission", nullable = false)
    private Boolean isSystemPermission = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", length = 20)
    private PermissionType permissionType;

    @Size(max = 200, message = "Resource path cannot exceed 200 characters")
    @Column(name = "resource_path", length = 200)
    private String resourcePath;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority cannot exceed 10")
    @Builder.Default
    @Column(name = "priority")
    private Integer priority = 5;

    @Size(max = 50, message = "Module cannot exceed 50 characters")
    @Column(name = "module", length = 50)
    private String module;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    @Builder.Default
    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    @Size(max = 500, message = "Conditions cannot exceed 500 characters")
    @Column(name = "conditions", length = 500)
    private String conditions;

    // Many-to-Many relationship with Role
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Custom constructors
    public Permission(String name, String description, String resource, String action) {
        this.name = name;
        this.description = description;
        this.resource = resource;
        this.action = action;
        this.isActive = true;
        this.isSystemPermission = false;
        this.permissionType = PermissionType.FUNCTIONAL;
        this.priority = 5;
        this.requiresApproval = false;
        this.roles = new HashSet<>();
    }

    public Permission(String name, String description, String resource, String action, PermissionType permissionType) {
        this(name, description, resource, action);
        this.permissionType = permissionType;
    }

    public Permission(String name, String description, String resource, String action, String resourcePath) {
        this(name, description, resource, action);
        this.resourcePath = resourcePath;
        this.permissionType = PermissionType.API;
    }

    public Permission(String name, String description, String resource, String action,
                      PermissionType permissionType, String module, String category) {
        this(name, description, resource, action, permissionType);
        this.module = module;
        this.category = category;
    }

    // Business methods
    public String getFullPermissionName() {
        return this.resource + ":" + this.action;
    }

    public boolean matches(String resource, String action) {
        return this.resource.equals(resource) && this.action.equals(action);
    }

    public boolean matchesPattern(String resourcePattern, String actionPattern) {
        return this.resource.matches(resourcePattern) && this.action.matches(actionPattern);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isHighPriority() {
        return this.priority != null && this.priority <= 3;
    }

    public boolean isLowPriority() {
        return this.priority != null && this.priority >= 7;
    }

    public String getDisplayName() {
        return this.name.replace("_", " ").toLowerCase();
    }

    public boolean belongsToModule(String moduleName) {
        return this.module != null && this.module.equalsIgnoreCase(moduleName);
    }

    public boolean belongsToCategory(String categoryName) {
        return this.category != null && this.category.equalsIgnoreCase(categoryName);
    }

    public boolean needsApproval() {
        return this.requiresApproval != null && this.requiresApproval;
    }

    public boolean hasConditions() {
        return this.conditions != null && !this.conditions.trim().isEmpty();
    }

    public boolean isReadOnlyPermission() {
        return this.action != null && (
                this.action.equals(Actions.READ) ||
                        this.action.equals(Actions.VIEW) ||
                        this.action.equals(Actions.EXPORT)
        );
    }

    public boolean isWritePermission() {
        return this.action != null && (
                this.action.equals(Actions.CREATE) ||
                        this.action.equals(Actions.UPDATE) ||
                        this.action.equals(Actions.DELETE) ||
                        this.action.equals(Actions.MODIFY)
        );
    }

    public boolean isAdministrativePermission() {
        return this.action != null && (
                this.action.equals(Actions.MANAGE) ||
                        this.action.equals(Actions.APPROVE) ||
                        this.action.equals(Actions.REJECT) ||
                        this.action.equals(Actions.ASSIGN)
        );
    }

    // Static factory methods
    public static Permission createReadPermission(String resource, String description) {
        return new Permission(
                resource + "_READ",
                description,
                resource,
                Actions.READ,
                PermissionType.FUNCTIONAL
        );
    }

    public static Permission createWritePermission(String resource, String description) {
        return new Permission(
                resource + "_WRITE",
                description,
                resource,
                Actions.UPDATE,
                PermissionType.FUNCTIONAL
        );
    }

    public static Permission createManagePermission(String resource, String description) {
        return new Permission(
                resource + "_MANAGE",
                description,
                resource,
                Actions.MANAGE,
                PermissionType.FUNCTIONAL
        );
    }

    public static Permission createApiPermission(String resource, String action, String path) {
        return new Permission(
                resource + "_" + action,
                "API permission for " + resource.toLowerCase() + " " + action.toLowerCase(),
                resource,
                action,
                path
        );
    }

    @Getter
    public enum PermissionType {
        FUNCTIONAL("Functional Permission", "Functional access to system features"),
        API("API Permission", "REST API endpoint access"),
        UI("UI Permission", "User interface component access"),
        DATA("Data Permission", "Database record access"),
        REPORT("Report Permission", "Report generation and viewing"),
        SYSTEM("System Permission", "System administration access"),
        EMERGENCY("Emergency Permission", "Emergency override access");

        private final String displayName;
        private final String description;

        PermissionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }

    // Common permission actions
    public static class Actions {
        public static final String CREATE = "CREATE";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String VIEW = "VIEW";
        public static final String MANAGE = "MANAGE";
        public static final String APPROVE = "APPROVE";
        public static final String REJECT = "REJECT";
        public static final String EXPORT = "EXPORT";
        public static final String IMPORT = "IMPORT";
        public static final String ASSIGN = "ASSIGN";
        public static final String SCHEDULE = "SCHEDULE";
        public static final String CANCEL = "CANCEL";
        public static final String MODIFY = "MODIFY";
        public static final String ARCHIVE = "ARCHIVE";
        public static final String RESTORE = "RESTORE";
        public static final String PRINT = "PRINT";
        public static final String DOWNLOAD = "DOWNLOAD";
        public static final String UPLOAD = "UPLOAD";
        public static final String SEND = "SEND";
        public static final String RECEIVE = "RECEIVE";
        public static final String PROCESS = "PROCESS";
        public static final String VALIDATE = "VALIDATE";
        public static final String VERIFY = "VERIFY";
        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String REVIEW = "REVIEW";
        public static final String SUBMIT = "SUBMIT";
        public static final String WITHDRAW = "WITHDRAW";
        public static final String LOCK = "LOCK";
        public static final String UNLOCK = "UNLOCK";
        public static final String SUSPEND = "SUSPEND";
        public static final String ACTIVATE = "ACTIVATE";
        public static final String DEACTIVATE = "DEACTIVATE";
        public static final String CONFIGURE = "CONFIGURE";
        public static final String MONITOR = "MONITOR";
        public static final String AUDIT = "AUDIT";
        public static final String BACKUP = "BACKUP";
        public static final String RESTORE_BACKUP = "RESTORE_BACKUP";

        // Private constructor to prevent instantiation
        private Actions() {}

        // Utility methods
        public static boolean isReadAction(String action) {
            return READ.equals(action) || VIEW.equals(action) || EXPORT.equals(action);
        }

        public static boolean isWriteAction(String action) {
            return CREATE.equals(action) || UPDATE.equals(action) || DELETE.equals(action) || MODIFY.equals(action);
        }

        public static boolean isAdminAction(String action) {
            return MANAGE.equals(action) || APPROVE.equals(action) || REJECT.equals(action) ||
                    ASSIGN.equals(action) || CONFIGURE.equals(action);
        }
    }

    // Common resource types
    public static class Resources {
        // Core entities
        public static final String USER = "USER";
        public static final String PATIENT = "PATIENT";
        public static final String DOCTOR = "DOCTOR";
        public static final String NURSE = "NURSE";
        public static final String RECEPTIONIST = "RECEPTIONIST";
        public static final String LAB_TECHNICIAN = "LAB_TECHNICIAN";
        public static final String ADMIN = "ADMIN";

        // Clinical resources
        public static final String APPOINTMENT = "APPOINTMENT";
        public static final String MEDICAL_RECORD = "MEDICAL_RECORD";
        public static final String LAB_RESULT = "LAB_RESULT";
        public static final String PRESCRIPTION = "PRESCRIPTION";
        public static final String DIAGNOSIS = "DIAGNOSIS";
        public static final String TREATMENT_PLAN = "TREATMENT_PLAN";
        public static final String VITAL_SIGNS = "VITAL_SIGNS";
        public static final String MEDICAL_HISTORY = "MEDICAL_HISTORY";
        public static final String ALLERGY = "ALLERGY";
        public static final String MEDICATION = "MEDICATION";

        // Administrative resources
        public static final String BILLING = "BILLING";
        public static final String INVOICE = "INVOICE";
        public static final String PAYMENT = "PAYMENT";
        public static final String INSURANCE = "INSURANCE";
        public static final String DEPARTMENT = "DEPARTMENT";
        public static final String WARD = "WARD";
        public static final String ROOM = "ROOM";
        public static final String BED = "BED";

        // System resources
        public static final String ROLE = "ROLE";
        public static final String PERMISSION = "PERMISSION";
        public static final String SYSTEM = "SYSTEM";
        public static final String AUDIT = "AUDIT";
        public static final String REPORT = "REPORT";
        public static final String DASHBOARD = "DASHBOARD";
        public static final String NOTIFICATION = "NOTIFICATION";
        public static final String BACKUP = "BACKUP";
        public static final String CONFIGURATION = "CONFIGURATION";

        // Specialized resources
        public static final String PHARMACY = "PHARMACY";
        public static final String INVENTORY = "INVENTORY";
        public static final String EQUIPMENT = "EQUIPMENT";
        public static final String EMERGENCY = "EMERGENCY";
        public static final String SURGERY = "SURGERY";
        public static final String RADIOLOGY = "RADIOLOGY";
        public static final String PATHOLOGY = "PATHOLOGY";
        public static final String DISCHARGE = "DISCHARGE";
        public static final String ADMISSION = "ADMISSION";
        public static final String TRANSFER = "TRANSFER";

        // Private constructor to prevent instantiation
        private Resources() {}

        // Utility methods
        public static boolean isClinicalResource(String resource) {
            return APPOINTMENT.equals(resource) || MEDICAL_RECORD.equals(resource) ||
                    LAB_RESULT.equals(resource) || PRESCRIPTION.equals(resource) ||
                    DIAGNOSIS.equals(resource) || TREATMENT_PLAN.equals(resource);
        }

        public static boolean isAdministrativeResource(String resource) {
            return BILLING.equals(resource) || INVOICE.equals(resource) ||
                    PAYMENT.equals(resource) || INSURANCE.equals(resource) ||
                    DEPARTMENT.equals(resource) || WARD.equals(resource);
        }

        public static boolean isSystemResource(String resource) {
            return ROLE.equals(resource) || PERMISSION.equals(resource) ||
                    SYSTEM.equals(resource) || AUDIT.equals(resource) ||
                    CONFIGURATION.equals(resource) || BACKUP.equals(resource);
        }

        public static boolean isUserResource(String resource) {
            return USER.equals(resource) || PATIENT.equals(resource) ||
                    DOCTOR.equals(resource) || NURSE.equals(resource) ||
                    RECEPTIONIST.equals(resource) || LAB_TECHNICIAN.equals(resource) ||
                    ADMIN.equals(resource);
        }
    }

    // Permission categories for grouping
    public static class Categories {
        public static final String CLINICAL = "Clinical";
        public static final String ADMINISTRATIVE = "Administrative";
        public static final String FINANCIAL = "Financial";
        public static final String SYSTEM = "System";
        public static final String REPORTING = "Reporting";
        public static final String EMERGENCY = "Emergency";
        public static final String PHARMACY = "Pharmacy";
        public static final String LABORATORY = "Laboratory";
        public static final String RADIOLOGY = "Radiology";
        public static final String NURSING = "Nursing";
        public static final String RECEPTION = "Reception";

        private Categories() {}
    }

    // Module names for organizing permissions
    public static class Modules {
        public static final String PATIENT_MANAGEMENT = "Patient Management";
        public static final String APPOINTMENT_SYSTEM = "Appointment System";
        public static final String EMR_SYSTEM = "EMR System";
        public static final String BILLING_SYSTEM = "Billing System";
        public static final String PHARMACY_SYSTEM = "Pharmacy System";
        public static final String LAB_SYSTEM = "Laboratory System";
        public static final String USER_MANAGEMENT = "User Management";
        public static final String REPORTING_SYSTEM = "Reporting System";
        public static final String SYSTEM_ADMINISTRATION = "System Administration";
        public static final String INVENTORY_MANAGEMENT = "Inventory Management";

        private Modules() {}
    }
}
