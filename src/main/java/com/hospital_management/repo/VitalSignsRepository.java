package com.hospital_management.repo;



import com.hospital_management.models.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long>, JpaSpecificationExecutor<VitalSigns> {

    List<VitalSigns> findByPatientIdOrderByRecordedDateDesc(Long patientId);

    @Query("SELECT v FROM VitalSigns v WHERE v.patientId = :patientId ORDER BY v.recordedDate DESC LIMIT 1")
    Optional<VitalSigns> findLatestForPatient(@Param("patientId") Long patientId);

    @Query("SELECT v FROM VitalSigns v WHERE v.patientId = :patientId " +
            "AND v.recordedDate BETWEEN :startDate AND :endDate " +
            "ORDER BY v.recordedDate DESC")
    List<VitalSigns> findByPatientAndDateRange(@Param("patientId") Long patientId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
}

