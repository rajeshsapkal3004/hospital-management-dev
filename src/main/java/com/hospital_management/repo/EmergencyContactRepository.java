package com.hospital_management.repo;


import com.hospital_management.models.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long>, JpaSpecificationExecutor<EmergencyContact> {

    List<EmergencyContact> findByPatientIdAndIsActiveTrue(Long patientId);
    Optional<EmergencyContact> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT e FROM EmergencyContact e WHERE e.patientId = :patientId " +
            "AND e.isPrimary = true AND e.isActive = true")
    Optional<EmergencyContact> findPrimaryContactForPatient(@Param("patientId") Long patientId);

    List<EmergencyContact> findByPatientIdOrderByIsPrimaryDescContactNameAsc(Long patientId);
}
