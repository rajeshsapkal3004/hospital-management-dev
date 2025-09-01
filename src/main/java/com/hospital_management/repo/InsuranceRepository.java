package com.hospital_management.repo;


import com.hospital_management.models.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long>, JpaSpecificationExecutor<Insurance> {

    List<Insurance> findByPatientIdAndIsActiveTrue(Long patientId);
    Optional<Insurance> findByIdAndPatientId(Long id, Long patientId);

    @Query("SELECT i FROM Insurance i WHERE i.patientId = :patientId " +
            "AND i.isPrimary = true AND i.isActive = true")
    Optional<Insurance> findPrimaryInsuranceForPatient(@Param("patientId") Long patientId);

    @Query("SELECT i FROM Insurance i WHERE i.expirationDate <= :date " +
            "AND i.isActive = true")
    List<Insurance> findExpiringInsurance(@Param("date") LocalDate date);

    List<Insurance> findByPatientIdOrderByIsPrimaryDescCreatedDateDesc(Long patientId);
}
