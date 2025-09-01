package com.hospital_management.repo;



import com.hospital_management.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    // Find payments by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find payments by patient ID
    List<Payment> findByPatientIdOrderByPaymentDateDesc(Long patientId);

    // Find payments by bill ID
    List<Payment> findByBillIdOrderByPaymentDateDesc(Long billId);

    // Find payments by status
    List<Payment> findByStatusOrderByPaymentDateDesc(Payment.PaymentStatus status);

    // Find payments in date range
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findPaymentsInDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Find successful payments for patient
    @Query("SELECT p FROM Payment p WHERE p.patientId = :patientId " +
            "AND p.status = 'COMPLETED' " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findSuccessfulPaymentsForPatient(@Param("patientId") Long patientId);

    // Calculate total payments by patient
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.patientId = :patientId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaymentsByPatient(@Param("patientId") Long patientId);

    // Find failed payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' " +
            "AND p.paymentDate >= :since " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findFailedPaymentsSince(@Param("since") LocalDateTime since);

    // Find pending payments
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING') " +
            "ORDER BY p.createdDate ASC")
    List<Payment> findPendingPayments();

    // Calculate daily revenue
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE DATE(p.paymentDate) = :date AND p.status = 'COMPLETED'")
    BigDecimal calculateDailyRevenue(@Param("date") LocalDate date);

    // Find refunded payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'REFUNDED' " +
            "ORDER BY p.refundedDate DESC")
    List<Payment> findRefundedPayments();

    // Count payments by method
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p " +
            "WHERE p.status = 'COMPLETED' " +
            "AND p.paymentDate >= :since " +
            "GROUP BY p.paymentMethod")
    List<Object[]> countPaymentsByMethod(@Param("since") LocalDateTime since);

    // Find recent payments for bill
    @Query("SELECT p FROM Payment p WHERE p.bill.id = :billId " +
            "ORDER BY p.paymentDate DESC")
    List<Payment> findRecentPaymentsForBill(@Param("billId") Long billId);
}
