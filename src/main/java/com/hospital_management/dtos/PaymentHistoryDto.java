package com.hospital_management.dtos;


import com.hospital_management.models.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryDto {

    private Long paymentId;
    private Long billId;
    private String billNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String transactionId;
    private Payment.PaymentStatus status;
    private String statusDisplayName;
    private String notes;
    private String referenceNumber;
    private BigDecimal refundedAmount;
    private LocalDateTime refundedDate;
    private String receiptUrl;

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }
}
