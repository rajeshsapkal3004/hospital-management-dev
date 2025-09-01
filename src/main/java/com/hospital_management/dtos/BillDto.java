package com.hospital_management.dtos;



import com.hospital_management.models.Bill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDto {

    private Long id;
    private String billNumber;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long appointmentId;
    private LocalDate billDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String description;
    private Bill.BillStatus status;
    private Bill.BillType billType;
    private LocalDateTime paidDate;
    private String insuranceClaimNumber;
    private Boolean isInsuranceCovered;
    private BigDecimal insuranceAmount;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;

    // Computed fields
    private BigDecimal outstandingAmount;
    private String statusDisplayName;
    private String billTypeDisplayName;
    private Boolean isFullyPaid;
    private Boolean isOverdue;
    private BigDecimal paymentPercentage;
    private Integer daysPastDue;
    private List<PaymentSummaryDto> recentPayments;

    // Helper methods
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    public String getBillTypeDisplayName() {
        return billType != null ? billType.getDisplayName() : null;
    }

    public BigDecimal getOutstandingAmount() {
        if (totalAmount == null || paidAmount == null) {
            return BigDecimal.ZERO;
        }
        return totalAmount.subtract(paidAmount);
    }

    public Boolean getIsFullyPaid() {
        if (totalAmount == null || paidAmount == null) {
            return false;
        }
        return paidAmount.compareTo(totalAmount) >= 0;
    }

    public Boolean getIsOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !getIsFullyPaid();
    }

    public Integer getDaysPastDue() {
        if (dueDate == null || !getIsOverdue()) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
}
