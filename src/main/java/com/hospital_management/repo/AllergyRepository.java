package com.hospital_management.repo;


import com.hospital_management.models.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long>, JpaSpecificationExecutor<Allergy> {

    List<Allergy> findByPatientIdAndIsActiveTrue(Long patientId);
    Optional<Allergy> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT a FROM Allergy a WHERE a.patientId = :patientId " +
            "AND a.severity = 'SEVERE' AND a.isActive = true")
    List<Allergy> findSevereAllergiesForPatient(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Allergy a WHERE a.patientId = :patientId " +
            "AND a.allergyType = :type AND a.isActive = true")
    List<Allergy> findByPatientIdAndType(@Param("patientId") Long patientId,
                                         @Param("type") String allergyType);
}
