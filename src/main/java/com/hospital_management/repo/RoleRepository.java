package com.hospital_management.repo;

import com.hospital_management.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Basic queries
    Optional<Role> findByName(Role.RoleName name);

    List<Role> findByNameIn(Set<Role.RoleName> names);

    List<Role> findByIsActiveTrue();

    List<Role> findByIsActiveFalse();

    List<Role> findByIsSystemRoleTrue();

    List<Role> findByIsSystemRoleFalse();

    // Hierarchy queries
    @Query("SELECT r FROM Role r WHERE r.hierarchyLevel <= :level AND r.isActive = true ORDER BY r.hierarchyLevel")
    List<Role> findRolesByMaxHierarchyLevel(@Param("level") Integer level);

    @Query("SELECT r FROM Role r WHERE r.hierarchyLevel >= :level AND r.isActive = true ORDER BY r.hierarchyLevel")
    List<Role> findRolesByMinHierarchyLevel(@Param("level") Integer level);

    @Query("SELECT r FROM Role r WHERE r.hierarchyLevel BETWEEN :minLevel AND :maxLevel AND r.isActive = true ORDER BY r.hierarchyLevel")
    List<Role> findRolesByHierarchyLevelRange(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    @Query("SELECT r FROM Role r ORDER BY r.hierarchyLevel ASC")
    List<Role> findAllOrderByHierarchyLevel();

    // Permission-related queries
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.name = :roleName")
    Optional<Role> findByNameWithPermissions(@Param("roleName") Role.RoleName roleName);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.isActive = true")
    List<Role> findActiveRolesWithPermissions();

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.id = :roleId")
    Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findRolesWithPermission(@Param("permissionId") Long permissionId);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findRolesWithPermissionName(@Param("permissionName") String permissionName);

    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) > 0")
    List<Role> findRolesWithPermissions();

    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) = 0")
    List<Role> findRolesWithoutPermissions();

    @Query("SELECT r FROM Role r WHERE SIZE(r.permissions) >= :minCount")
    List<Role> findRolesWithMinPermissions(@Param("minCount") int minCount);

    // User-related queries
    @Query("SELECT r FROM Role r JOIN FETCH r.users WHERE r.name = :roleName")
    Optional<Role> findByNameWithUsers(@Param("roleName") Role.RoleName roleName);

    @Query("SELECT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findRolesWithUsers();

    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findRolesWithoutUsers();

    @Query("SELECT r FROM Role r WHERE SIZE(r.users) >= :minCount")
    List<Role> findRolesWithMinUsers(@Param("minCount") int minCount);

    // Search queries
    @Query("SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Role> findByDescriptionContaining(@Param("keyword") String keyword);

    @Query("SELECT r FROM Role r WHERE " +
            "(:isActive IS NULL OR r.isActive = :isActive) AND " +
            "(:isSystemRole IS NULL OR r.isSystemRole = :isSystemRole) AND " +
            "(:minHierarchyLevel IS NULL OR r.hierarchyLevel >= :minHierarchyLevel) AND " +
            "(:maxHierarchyLevel IS NULL OR r.hierarchyLevel <= :maxHierarchyLevel)")
    List<Role> findRolesWithFilters(@Param("isActive") Boolean isActive,
                                    @Param("isSystemRole") Boolean isSystemRole,
                                    @Param("minHierarchyLevel") Integer minHierarchyLevel,
                                    @Param("maxHierarchyLevel") Integer maxHierarchyLevel);

    // Existence checks
    boolean existsByName(Role.RoleName name);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :name AND r.id != :id")
    boolean existsByNameAndIdNot(@Param("name") Role.RoleName name, @Param("id") Long id);

    // Update queries
    @Modifying
    @Query("UPDATE Role r SET r.isActive = :active WHERE r.id = :roleId")
    void updateActiveStatus(@Param("roleId") Long roleId, @Param("active") boolean active);

    @Modifying
    @Query("UPDATE Role r SET r.hierarchyLevel = :level WHERE r.id = :roleId")
    void updateHierarchyLevel(@Param("roleId") Long roleId, @Param("level") Integer level);

    @Modifying
    @Query("UPDATE Role r SET r.description = :description WHERE r.id = :roleId")
    void updateDescription(@Param("roleId") Long roleId, @Param("description") String description);

    @Modifying
    @Query("UPDATE Role r SET r.isSystemRole = :isSystemRole WHERE r.id = :roleId")
    void updateSystemRoleStatus(@Param("roleId") Long roleId, @Param("isSystemRole") boolean isSystemRole);

    // Count queries
    long countByIsActiveTrue();

    long countByIsActiveFalse();

    long countByIsSystemRoleTrue();

    long countByIsSystemRoleFalse();

    @Query("SELECT COUNT(r) FROM Role r WHERE SIZE(r.users) > 0")
    long countRolesWithUsers();

    @Query("SELECT COUNT(r) FROM Role r WHERE SIZE(r.permissions) > 0")
    long countRolesWithPermissions();

    @Query("SELECT COUNT(r) FROM Role r WHERE r.hierarchyLevel <= :level")
    long countRolesByMaxHierarchyLevel(@Param("level") Integer level);

    // Statistical queries
    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.name ORDER BY COUNT(u) DESC")
    List<Object[]> getRoleUserCounts();

    @Query("SELECT r.name, COUNT(p) FROM Role r LEFT JOIN r.permissions p GROUP BY r.name ORDER BY COUNT(p) DESC")
    List<Object[]> getRolePermissionCounts();

    @Query("SELECT r.hierarchyLevel, COUNT(r) FROM Role r WHERE r.hierarchyLevel IS NOT NULL GROUP BY r.hierarchyLevel ORDER BY r.hierarchyLevel")
    List<Object[]> getRoleCountByHierarchyLevel();

    @Query("SELECT AVG(r.hierarchyLevel) FROM Role r WHERE r.hierarchyLevel IS NOT NULL")
    Double getAverageHierarchyLevel();

    @Query("SELECT MIN(r.hierarchyLevel), MAX(r.hierarchyLevel) FROM Role r WHERE r.hierarchyLevel IS NOT NULL")
    List<Object[]> getHierarchyLevelRange();

    // Permission assignment queries
    @Query("SELECT COUNT(DISTINCT p) FROM Role r JOIN r.permissions p WHERE r.isActive = true")
    long countDistinctPermissionsInActiveRoles();

    @Query("SELECT p.name, COUNT(r) FROM Role r JOIN r.permissions p WHERE r.isActive = true GROUP BY p.name ORDER BY COUNT(r) DESC")
    List<Object[]> getPermissionUsageByActiveRoles();

    // Role dependency queries
    @Query("SELECT r1.name, r2.name FROM Role r1, Role r2 WHERE r1.hierarchyLevel < r2.hierarchyLevel AND r1.isActive = true AND r2.isActive = true")
    List<Object[]> getRoleHierarchyRelationships();

    // Bulk operations
    @Modifying
    @Query("UPDATE Role r SET r.isActive = :active WHERE r.isSystemRole = :isSystemRole")
    void updateActiveStatusBySystemRole(@Param("active") boolean active, @Param("isSystemRole") boolean isSystemRole);

    @Modifying
    @Query("UPDATE Role r SET r.isActive = false WHERE SIZE(r.users) = 0 AND r.isSystemRole = false")
    void deactivateUnusedNonSystemRoles();
}
