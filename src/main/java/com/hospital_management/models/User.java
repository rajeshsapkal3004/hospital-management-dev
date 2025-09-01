package com.hospital_management.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name="users",uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type",discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"password"})
@SuperBuilder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3,max = 50,message = "Username must be between 3 and 50 characters")
    @Pattern(regexp ="^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens" )
    @Column(name = "username", unique = true,nullable = false,length = 50)
    private String username;


    @NotBlank(message = "Email is required")
    @Email(message = "Please Provide a valid email address")
    @Size(max = 100,message = "Email cannot exceed 100 characters")
    @Column(name = "email", unique = true,nullable = false,length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters long")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number (10-15 digits)")
    @Column(name = "phone",length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender",length = 10)
    private Gender gender;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;

    @Pattern(regexp = "^[0-9]{5,10}$", message = "Please provide a valid postal code (5-10 digits)")
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Size(max = 50, message = "City cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "City can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "city", length = 50)
    private String city;

    @Size(max = 50, message = "State cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "State can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "state", length = 50)
    private String state;

    @Size(max = 50, message = "Country cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Country can only contain letters, spaces, apostrophes, and hyphens")
    @Column(name = "country", length = 50)
    private String country;

    // Account status fields
    @Builder.Default
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @Builder.Default
    @Column(name = "account_expired", nullable = false)
    private Boolean accountExpired = false;

    @Builder.Default
    @Column(name = "credentials_expired", nullable = false)
    private Boolean credentialsExpired = false;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Builder.Default
    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    // Login tracking
    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Builder.Default
    @Min(value = 0, message = "Failed login attempts cannot be negative")
    @Max(value = 10, message = "Failed login attempts cannot exceed 10")
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "password_changed_date")
    private LocalDateTime passwordChangedDate;

    @Column(name = "account_locked_date")
    private LocalDateTime accountLockedDate;

    // Many-to-Many relationship with Role
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Audit fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    /**
     * Additional profile data as JSON
     */
    @Column(name = "profile_data", columnDefinition = "TEXT")
    private String profileDataJson;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Builder.Default
    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;


    // Custom constructors
    public User(String username, String email, String password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountLocked = false;
        this.accountExpired = false;
        this.credentialsExpired = false;
        this.enabled = true;
        this.emailVerified = false;
        this.phoneVerified = false;
        this.failedLoginAttempts = 0;
        this.roles = new HashSet<>();
    }

    // Business methods
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }

    public boolean hasAnyRole(Set<String> roleNames) {
        return this.roles.stream()
                .anyMatch(role -> roleNames.contains(role.getName().name()));
    }

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void lockAccount() {
        this.accountLocked = true;
        this.accountLockedDate = LocalDateTime.now();
    }

    public void unlockAccount() {
        this.accountLocked = false;
        this.accountLockedDate = null;
        this.failedLoginAttempts = 0;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount();
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
        resetFailedLoginAttempts();
    }

    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        OTHER("Other"),
        PREFER_NOT_TO_SAY("Prefer not to say");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public boolean isAccountLocked() {
        return this.accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
        if (accountLocked) {
            this.accountLockedDate = LocalDateTime.now();
        } else {
            this.accountLockedDate = null;
        }
    }

    /**
     * Check if account is expired
     * @return true if account is expired, false otherwise
     */
    public boolean isAccountExpired() {
        return this.accountExpired;
    }

    /**
     * Set account expired status
     * @param accountExpired true if expired, false otherwise
     */
    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    /**
     * Check if credentials are expired
     * @return true if credentials are expired, false otherwise
     */
    public boolean isCredentialsExpired() {
        return this.credentialsExpired;
    }

    /**
     * Set credentials expired status
     * @param credentialsExpired true if expired, false otherwise
     */
    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    /**
     * Get profile data as Map
     * @return Map of profile data
     */
    @Transient
    public Map<String, Object> getProfileData() {
        if (profileDataJson == null || profileDataJson.isEmpty()) {
            return new HashMap<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(profileDataJson, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Set profile data from Map
     * @param profileData Map of profile data
     */
    @Transient
    public void setProfileData(Map<String, Object> profileData) {
        if (profileData == null || profileData.isEmpty()) {
            this.profileDataJson = null;
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.profileDataJson = objectMapper.writeValueAsString(profileData);
        } catch (Exception e) {
            this.profileDataJson = null;
        }
    }

    /**
     * Add profile data entry
     * @param key The key
     * @param value The value
     */
    public void addProfileData(String key, Object value) {
        Map<String, Object> currentData = getProfileData();
        currentData.put(key, value);
        setProfileData(currentData);
    }

    /**
     * Remove profile data entry
     * @param key The key to remove
     */
    public void removeProfileData(String key) {
        Map<String, Object> currentData = getProfileData();
        currentData.remove(key);
        setProfileData(currentData);
    }

    /**
     * Get specific profile data value
     * @param key The key
     * @return The value or null if not found
     */
    public Object getProfileDataValue(String key) {
        return getProfileData().get(key);
    }






}
