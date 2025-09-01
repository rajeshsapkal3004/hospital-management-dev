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
public class PaymentSummaryDto {

    private Long paymentId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String status;
    private String transactionId;
}

