package com.hospital_management.dtos;



import com.hospital_management.models.PrescriptionItem;
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
public class PrescriptionItemDto {

    private Long id;
    private String medicationName;
    private String genericName;
    private String brandName;
    private String strength;
    private String dosageForm;
    private Integer quantity;
    private String unit;
    private String dosageInstructions;
    private String frequency;
    private String duration;
    private String route;
    private String specialInstructions;
    private String warnings;
    private PrescriptionItem.SubstitutionPolicy substitutionAllowed;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean isDispensed;
    private Integer dispensedQuantity;
    private Integer remainingQuantity;
    private LocalDateTime createdDate;

    // Computed fields
    private String substitutionDisplayName;
    private Boolean isFullyDispensed;
    private Boolean isPartiallyDispensed;
    private String dispensingStatus;

    // Helper methods
    public String getSubstitutionDisplayName() {
        return substitutionAllowed != null ? substitutionAllowed.getDisplayName() : null;
    }

    public Boolean getIsFullyDispensed() {
        return isDispensed != null && isDispensed &&
                dispensedQuantity != null && quantity != null &&
                dispensedQuantity >= quantity;
    }

    public Boolean getIsPartiallyDispensed() {
        return dispensedQuantity != null && dispensedQuantity > 0 &&
                quantity != null && dispensedQuantity < quantity;
    }

    public String getDispensingStatus() {
        if (getIsFullyDispensed()) return "Fully Dispensed";
        if (getIsPartiallyDispensed()) return "Partially Dispensed";
        return "Not Dispensed";
    }
}
