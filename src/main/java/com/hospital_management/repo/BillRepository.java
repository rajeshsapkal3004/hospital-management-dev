package com.hospital_management.repo;


import com.hospital_management.models.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

    // Find bill by ID and patient ID for security
    Optional<Bill> findByIdAndPatientId(Long id, Long patientId);

    // Find bills by patient ID
    List<Bill> findByPatientIdOrderByBillDateDesc(Long patientId);

    // Find outstanding bills (not fully paid)
    @Query("SELECT b FROM Bill b WHERE b.patientId = :patientId " +
            "AND b.status IN ('PENDING', 'PARTIAL_PAID', 'OVERDUE') " +
            "ORDER BY b.dueDate ASC")
    List<Bill> findOutstandingBills(@Param("patientId") Long patientId);

    // Find overdue bills
    @Query("SELECT b FROM Bill b WHERE b.dueDate < :currentDate " +
            "AND b.status IN ('PENDING', 'PARTIAL_PAID') " +
            "ORDER BY b.dueDate ASC")
    List<Bill> findOverdueBills(@Param("currentDate") LocalDate currentDate);

    // Find bills by status
    List<Bill> findByStatusOrderByBillDateDesc(Bill.BillStatus status);

    // Find bills by bill number
    Optional<Bill> findByBillNumber(String billNumber);

    // Find bills by appointment ID
    List<Bill> findByAppointmentId(Long appointmentId);

    // Find bills in date range
    @Query("SELECT b FROM Bill b WHERE b.billDate BETWEEN :startDate AND :endDate " +
            "ORDER BY b.billDate DESC")
    List<Bill> findBillsInDateRange(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    // Get total outstanding amount for patient
    @Query("SELECT COALESCE(SUM(b.totalAmount - b.paidAmount), 0) FROM Bill b " +
            "WHERE b.patientId = :patientId AND b.status != 'PAID'")
    BigDecimal getTotalOutstandingAmount(@Param("patientId") Long patientId);

    // Get bills by type
    List<Bill> findByBillTypeAndPatientIdOrderByBillDateDesc(Bill.BillType billType, Long patientId);

    // Count bills by status
    @Query("SELECT b.status, COUNT(b) FROM Bill b GROUP BY b.status")
    List<Object[]> countBillsByStatus();

    // Find recent bills for patient
    @Query("SELECT b FROM Bill b WHERE b.patientId = :patientId " +
            "ORDER BY b.createdDate DESC")
    List<Bill> findRecentBillsForPatient(@Param("patientId") Long patientId);

    // Find bills requiring insurance processing
    @Query("SELECT b FROM Bill b WHERE b.isInsuranceCovered = true " +
            "AND b.insuranceClaimNumber IS NOT NULL " +
            "AND b.status = 'PENDING'")
    List<Bill> findBillsRequiringInsuranceProcessing();

    // Calculate revenue by date range
    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Bill b " +
            "WHERE b.billDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenue(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    // Find bills with partial payments
    @Query("SELECT b FROM Bill b WHERE b.paidAmount > 0 " +
            "AND b.paidAmount < b.totalAmount " +
            "ORDER BY b.lastModifiedDate DESC")
    List<Bill> findBillsWithPartialPayments();
}
