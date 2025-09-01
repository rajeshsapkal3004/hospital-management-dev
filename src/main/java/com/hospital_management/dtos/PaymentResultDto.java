package com.hospital_management.dtos;



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
public class PaymentResultDto {

    private Long paymentId;
    private String transactionId;
    private String status;
    private String message;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private LocalDateTime paymentDate;
    private String receiptUrl;
    private Boolean requiresApproval;
    private String approvalReference;
    private String paymentMethod;
    private String gatewayResponse;
    private String errorCode;
    private String errorMessage;
}
