package com.hospital_management.repo;

import com.hospital_management.models.Role;
import com.hospital_management.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // Basic authentication queries
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query("SELECT u FROM User u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.enabled = true")
    Optional<User> findByUsernameOrEmailAndEnabled(@Param("usernameOrEmail") String usernameOrEmail);

    // Existence checks
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") Role.RoleName roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name IN :roleNames")
    List<User> findByRoleNames(@Param("roleNames") Set<Role.RoleName> roleNames);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("roleName") Role.RoleName roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.enabled = true AND u.accountLocked = false")
    List<User> findActiveNonLockedUsersByRole(@Param("roleName") Role.RoleName roleName);

    // Status-based queries
    List<User> findByEnabledTrue();

    List<User> findByEnabledFalse();

    List<User> findByAccountLockedTrue();

    List<User> findByAccountLockedFalse();

    List<User> findByEmailVerifiedFalse();

    List<User> findByPhoneVerifiedFalse();

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.accountLocked = false AND u.accountExpired = false")
    List<User> findAllActiveUsers();

    // Login tracking queries
    @Query("SELECT u FROM User u WHERE u.lastLoginDate < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.lastLoginDate IS NULL")
    List<User> findUsersWhoNeverLoggedIn();

    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
    List<User> findUsersWithFailedAttempts(@Param("attempts") Integer attempts);

    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts AND u.accountLocked = false")
    List<User> findUsersAtRiskOfLocking(@Param("attempts") Integer attempts);

    // Search and filter queries
    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> findUsersWithFilters(@Param("firstName") String firstName,
                                    @Param("lastName") String lastName,
                                    @Param("email") String email,
                                    @Param("username") String username,
                                    @Param("enabled") Boolean enabled,
                                    Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameOrUsernameOrEmailContaining(@Param("name") String name);

    // Geographic queries
    List<User> findByCity(String city);

    List<User> findByState(String state);

    List<User> findByCountry(String country);

    @Query("SELECT u FROM User u WHERE u.city = :city AND u.state = :state")
    List<User> findByCityAndState(@Param("city") String city, @Param("state") String state);

    // Update queries
    @Modifying
    @Query("UPDATE User u SET u.accountLocked = :locked, u.accountLockedDate = :lockedDate WHERE u.id = :userId")
    void updateAccountLockStatus(@Param("userId") Long userId,
                                 @Param("locked") boolean locked,
                                 @Param("lockedDate") LocalDateTime lockedDate);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") Long userId, @Param("attempts") Integer attempts);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = :loginDate, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void updateLastLoginDate(@Param("userId") Long userId, @Param("loginDate") LocalDateTime loginDate);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = true WHERE u.id = :userId")
    void markPhoneAsVerified(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateEnabledStatus(@Param("userId") Long userId, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.passwordChangedDate = :changedDate WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId,
                        @Param("password") String password,
                        @Param("changedDate") LocalDateTime changedDate);

    // Count queries
    long countByEnabledTrue();

    long countByAccountLockedTrue();

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") Role.RoleName roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdDate >= :date")
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginDate >= :date")
    long countUsersLoggedInAfter(@Param("date") LocalDateTime date);

    // Statistical queries
    @Query("SELECT u.city, COUNT(u) FROM User u WHERE u.city IS NOT NULL GROUP BY u.city ORDER BY COUNT(u) DESC")
    List<Object[]> getUserCountByCity();

    @Query("SELECT u.state, COUNT(u) FROM User u WHERE u.state IS NOT NULL GROUP BY u.state ORDER BY COUNT(u) DESC")
    List<Object[]> getUserCountByState();

    @Query("SELECT u.gender, COUNT(u) FROM User u WHERE u.gender IS NOT NULL GROUP BY u.gender")
    List<Object[]> getUserCountByGender();


    // NEW MISSING METHODS:

    /**
     * Check if any user exists with the given role
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.roles r WHERE r.name = :roleName")
    boolean existsByRoles_Name(@Param("roleName") Role.RoleName roleName);

    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoles_Name(@Param("roleName") Role.RoleName roleName);

    /**
     * Count users by role name
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoles_Name(@Param("roleName") Role.RoleName roleName);


    /**
     * Find users by username containing (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);

    /**
     * Find users by email containing (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);

    /**
     * Find users by first name and last name containing
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
    List<User> findByFirstNameAndLastNameContaining(@Param("firstName") String firstName,
                                                    @Param("lastName") String lastName);

    /**
     * Search users with multiple filters
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> searchUsers(@Param("firstName") String firstName,
                           @Param("lastName") String lastName,
                           @Param("email") String email,
                           @Param("username") String username,
                           @Param("enabled") Boolean enabled,
                           Pageable pageable);



    /**
     * Count disabled users
     */
    Long countByEnabledFalse();

    /**
     * Find users with account locked
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true")
    List<User> findLockedAccounts();

    /**
     * Count locked accounts
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountLocked = true")
    Long countLockedAccounts();

    /**
     * Get user count by role
     */
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    List<Object[]> getUserCountByRole();

    // For advanced filtering
    Page<User> findAll(Specification<User> spec, Pageable pageable);

}

