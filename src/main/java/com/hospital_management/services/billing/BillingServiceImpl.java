package com.hospital_management.services.billing;


import com.hospital_management.dtos.*;
import com.hospital_management.exception.BillNotFoundException;
import com.hospital_management.exception.PaymentProcessingException;
import com.hospital_management.mapper.BillMapper;
import com.hospital_management.mapper.PaymentMapper;
import com.hospital_management.models.Bill;
import com.hospital_management.models.Payment;
import com.hospital_management.repo.BillRepository;
import com.hospital_management.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BillMapper billMapper;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<BillDto> getPatientBills(Long patientId, LocalDate startDate, LocalDate endDate,
                                         String status, Pageable pageable) {
        log.debug("Fetching bills for patient: {}", patientId);

        Specification<Bill> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("billDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("billDate"), endDate));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"),
                        Bill.BillStatus.valueOf(status.toUpperCase())));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Bill> bills = billRepository.findAll(spec, pageable);
        return bills.map(billMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BillDto getPatientBill(Long patientId, Long billId) {
        log.debug("Fetching bill {} for patient: {}", billId, patientId);

        Bill bill = billRepository.findByIdAndPatientId(billId, patientId)
                .orElseThrow(() -> new BillNotFoundException(billId));

        return billMapper.toDto(bill);
    }

    @Override
    @Transactional
    public PaymentResultDto processPayment(Long billId, PaymentDto paymentDto) {
        log.info("Processing payment for bill: {}", billId);

        try {
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new BillNotFoundException(billId));

            if (bill.getStatus() == Bill.BillStatus.PAID) {
                throw new PaymentProcessingException("Bill is already paid");
            }

            // Validate payment amount
            BigDecimal outstandingAmount = bill.getTotalAmount().subtract(bill.getPaidAmount());
            if (paymentDto.getAmount().compareTo(outstandingAmount) > 0) {
                throw new PaymentProcessingException("Payment amount exceeds outstanding balance");
            }

            // Create payment record
            Payment payment = Payment.builder()
                    .bill(bill)
                    .patientId(paymentDto.getPatientId())
                    .amount(paymentDto.getAmount())
                    .paymentMethod(paymentDto.getPaymentMethod())
                    .paymentDate(LocalDateTime.now())
                    .transactionId(generateTransactionId())
                    .status(Payment.PaymentStatus.COMPLETED)
                    .notes(paymentDto.getNotes())
                    .build();

            // Update bill
            BigDecimal newPaidAmount = bill.getPaidAmount().add(paymentDto.getAmount());
            bill.setPaidAmount(newPaidAmount);

            if (newPaidAmount.compareTo(bill.getTotalAmount()) >= 0) {
                bill.setStatus(Bill.BillStatus.PAID);
                bill.setPaidDate(LocalDateTime.now());
            } else {
                bill.setStatus(Bill.BillStatus.PARTIAL_PAID);
            }

            Payment savedPayment = paymentRepository.save(payment);
            billRepository.save(bill);

            return PaymentResultDto.builder()
                    .paymentId(savedPayment.getId())
                    .transactionId(savedPayment.getTransactionId())
                    .status("SUCCESS")
                    .message("Payment processed successfully")
                    .paidAmount(paymentDto.getAmount())
                    .outstandingAmount(bill.getTotalAmount().subtract(bill.getPaidAmount()))
                    .build();

        } catch (Exception e) {
            log.error("Payment processing failed", e);
            throw new PaymentProcessingException("Payment failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillDto> getOutstandingBills(Long patientId) {
        log.debug("Fetching outstanding bills for patient: {}", patientId);

        List<Bill> bills = billRepository.findOutstandingBills(patientId);

        return bills.stream()
                .map(billMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentHistoryDto> getPatientPaymentHistory(Long patientId, LocalDate startDate,
                                                            LocalDate endDate, Pageable pageable) {
        log.debug("Fetching payment history for patient: {}", patientId);

        Specification<Payment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("patientId"), patientId));

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("paymentDate")), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.function("DATE", LocalDate.class, root.get("paymentDate")), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        return payments.map(this::convertToPaymentHistory);
    }

    @Override
    @Transactional
    public BillDto createBill(CreateBillDto billDto) {
        log.info("Creating bill for patient: {}", billDto.getPatientId());

        try {
            Bill bill = Bill.builder()
                    .patientId(billDto.getPatientId())
                    .appointmentId(billDto.getAppointmentId())
                    .billNumber(generateBillNumber())
                    .billDate(LocalDate.now())
                    .totalAmount(billDto.getTotalAmount())
                    .paidAmount(BigDecimal.ZERO)
                    .status(Bill.BillStatus.PENDING)
                    .description(billDto.getDescription())
                    .dueDate(billDto.getDueDate())
                    .build();

            Bill saved = billRepository.save(bill);
            return billMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error creating bill", e);
            throw new RuntimeException("Failed to create bill: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BillDto updateBill(Long billId, UpdateBillDto updateDto) {
        log.info("Updating bill: {}", billId);

        try {
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new BillNotFoundException(billId));

            bill.setTotalAmount(updateDto.getTotalAmount());
            bill.setDescription(updateDto.getDescription());
            bill.setDueDate(updateDto.getDueDate());

            Bill saved = billRepository.save(bill);
            return billMapper.toDto(saved);

        } catch (Exception e) {
            log.error("Error updating bill", e);
            throw new RuntimeException("Failed to update bill: " + e.getMessage());
        }
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateBillNumber() {
        return "BILL-" + System.currentTimeMillis();
    }

    private PaymentHistoryDto convertToPaymentHistory(Payment payment) {
        return PaymentHistoryDto.builder()
                .paymentId(payment.getId())
                .billId(payment.getBill().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .build();
    }
}
