package com.hospital_management.services.billing;



import com.hospital_management.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {
    Page<BillDto> getPatientBills(Long patientId, LocalDate startDate, LocalDate endDate, String status, Pageable pageable);
    BillDto getPatientBill(Long patientId, Long billId);
    PaymentResultDto processPayment(Long billId, PaymentDto paymentDto);
    List<BillDto> getOutstandingBills(Long patientId);
    Page<PaymentHistoryDto> getPatientPaymentHistory(Long patientId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    BillDto createBill(CreateBillDto billDto);
    BillDto updateBill(Long billId, UpdateBillDto updateDto);
}

