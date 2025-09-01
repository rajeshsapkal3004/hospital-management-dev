package com.hospital_management.repo;

import com.hospital_management.models.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Basic queries
    Optional<Permission> findByName(String name);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByResourceAndAction(String resource, String action);

    List<Permission> findByIsActiveTrue();

    List<Permission> findByIsSystemPermissionTrue();

    List<Permission> findByPermissionType(Permission.PermissionType permissionType);

    List<Permission> findByModule(String module);

    List<Permission> findByCategory(String category);

    List<Permission> findByRequiresApprovalTrue();

    // Advanced queries
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.isActive = true")
    List<Permission> findActivePermissionsByResource(@Param("resource") String resource);

    @Query("SELECT p FROM Permission p WHERE p.module = :module AND p.isActive = true")
    List<Permission> findActivePermissionsByModule(@Param("module") String module);

    @Query("SELECT p FROM Permission p WHERE p.category = :category AND p.isActive = true")
    List<Permission> findActivePermissionsByCategory(@Param("category") String category);

    @Query("SELECT p FROM Permission p WHERE p.priority <= :priority AND p.isActive = true")
    List<Permission> findHighPriorityPermissions(@Param("priority") Integer priority);

    @Query("SELECT p FROM Permission p WHERE p.action IN :actions AND p.isActive = true")
    List<Permission> findPermissionsByActions(@Param("actions") Set<String> actions);

    @Query("SELECT p FROM Permission p WHERE p.resource IN :resources AND p.isActive = true")
    List<Permission> findPermissionsByResources(@Param("resources") Set<String> resources);

    // Distinct queries
    @Query("SELECT DISTINCT p.resource FROM Permission p WHERE p.isActive = true ORDER BY p.resource")
    List<String> findAllActiveResources();

    @Query("SELECT DISTINCT p.action FROM Permission p WHERE p.resource = :resource AND p.isActive = true ORDER BY p.action")
    List<String> findActionsByResource(@Param("resource") String resource);

    @Query("SELECT DISTINCT p.module FROM Permission p WHERE p.module IS NOT NULL AND p.isActive = true ORDER BY p.module")
    List<String> findAllActiveModules();

    @Query("SELECT DISTINCT p.category FROM Permission p WHERE p.category IS NOT NULL AND p.isActive = true ORDER BY p.category")
    List<String> findAllActiveCategories();

    // Search queries
    @Query("SELECT p FROM Permission p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:resource IS NULL OR p.resource = :resource) AND " +
            "(:action IS NULL OR p.action = :action) AND " +
            "(:module IS NULL OR p.module = :module) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "p.isActive = true")
    Page<Permission> findPermissionsWithFilters(@Param("name") String name,
                                                @Param("resource") String resource,
                                                @Param("action") String action,
                                                @Param("module") String module,
                                                @Param("category") String category,
                                                Pageable pageable);

    // Role-based queries
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId AND p.isActive = true")
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName AND p.isActive = true")
    List<Permission> findPermissionsByRoleName(@Param("roleName") String roleName);

    // Existence checks
    boolean existsByName(String name);

    boolean existsByResourceAndAction(String resource, String action);

    boolean existsByResourceAndActionAndIsActiveTrue(String resource, String action);

    // Count queries
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.resource = :resource AND p.isActive = true")
    Long countActivePermissionsByResource(@Param("resource") String resource);

    @Query("SELECT COUNT(p) FROM Permission p WHERE p.module = :module AND p.isActive = true")
    Long countActivePermissionsByModule(@Param("module") String module);

    // Update queries
    @Modifying
    @Query("UPDATE Permission p SET p.isActive = :active WHERE p.id = :id")
    void updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Modifying
    @Query("UPDATE Permission p SET p.priority = :priority WHERE p.id = :id")
    void updatePriority(@Param("id") Long id, @Param("priority") Integer priority);

    @Modifying
    @Query("UPDATE Permission p SET p.module = :module WHERE p.resource = :resource")
    void updateModuleByResource(@Param("resource") String resource, @Param("module") String module);

    @Modifying
    @Query("UPDATE Permission p SET p.category = :category WHERE p.resource = :resource")
    void updateCategoryByResource(@Param("resource") String resource, @Param("category") String category);

    // Update description method
    @Modifying
    @Query("UPDATE Permission p SET p.description = :description WHERE p.id = :permissionId")
    void updateDescription(@Param("permissionId") Long permissionId, @Param("description") String description);

    // Count active permissions
    long countByIsActiveTrue();

    // Get permission usage by active roles
    @Query("SELECT p.name, COUNT(r) FROM Permission p LEFT JOIN p.roles r WHERE r.isActive = true GROUP BY p.name ORDER BY COUNT(r) DESC")
    List<Object[]> getPermissionUsageByActiveRoles();


    List<Permission> findByModuleAndCategory(String module, String category);
    // To this:



}
