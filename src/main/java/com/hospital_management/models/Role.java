package com.hospital_management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"users", "permissions"})
@ToString(exclude = {"users", "permissions"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Role name is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 30, unique = true, nullable = false)
    private RoleName name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_system_role", nullable = false)
    private Boolean isSystemRole = false;

    @Min(value = 1, message = "Hierarchy level must be at least 1")
    @Max(value = 10, message = "Hierarchy level cannot exceed 10")
    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;

    // Many-to-Many relationship with User
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    // Many-to-Many relationship with Permission
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Custom constructors
    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.isSystemRole = false;
        this.hierarchyLevel = name.getDefaultHierarchyLevel();
        this.users = new HashSet<>();
        this.permissions = new HashSet<>();
    }

    public Role(RoleName name, String description, Integer hierarchyLevel) {
        this(name, description);
        this.hierarchyLevel = hierarchyLevel;
    }

    // Business methods
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    public boolean hasPermission(String permissionName) {
        return this.permissions.stream()
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }

    public boolean hasPermission(String resource, String action) {
        return this.permissions.stream()
                .anyMatch(permission ->
                        permission.getResource().equals(resource) &&
                                permission.getAction().equals(action));
    }

    public Set<String> getPermissionNames() {
        Set<String> permissionNames = new HashSet<>();
        for (Permission permission : this.permissions) {
            permissionNames.add(permission.getName());
        }
        return permissionNames;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    @Getter
    public enum RoleName {
        ADMIN("Administrator", "Full system access and administration", 1),
        DOCTOR("Doctor", "Medical records and patient care access", 2),
        NURSE("Nurse", "Patient monitoring and care tasks", 3),
        RECEPTIONIST("Receptionist", "Appointment and registration management", 4),
        PATIENT("Patient", "Personal health records access", 5),
        LAB_TECHNICIAN("Lab Technician", "Laboratory results and tests management", 3);

        private final String displayName;
        private final String description;
        private final Integer defaultHierarchyLevel;

        RoleName(String displayName, String description, Integer defaultHierarchyLevel) {
            this.displayName = displayName;
            this.description = description;
            this.defaultHierarchyLevel = defaultHierarchyLevel;
        }
    }

}
