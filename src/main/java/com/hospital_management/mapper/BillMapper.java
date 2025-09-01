package com.hospital_management.mapper;


import com.hospital_management.dtos.BillDto;
import com.hospital_management.models.Bill;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillMapper {

    BillMapper INSTANCE = Mappers.getMapper(BillMapper.class);

    @Mapping(target = "patientName", expression = "java(bill.getPatient().getFirstName() + \" \" + bill.getPatient().getLastName())")
    @Mapping(target = "patientEmail", source = "patient.email")
    @Mapping(target = "outstandingAmount", expression = "java(bill.getOutstandingAmount())")
    @Mapping(target = "statusDisplayName", expression = "java(bill.getStatus().getDisplayName())")
    @Mapping(target = "billTypeDisplayName", expression = "java(bill.getBillType().getDisplayName())")
    @Mapping(target = "isFullyPaid", expression = "java(bill.isFullyPaid())")
    @Mapping(target = "isOverdue", expression = "java(bill.isOverdue())")
    @Mapping(target = "paymentPercentage", expression = "java(bill.getPaymentPercentage())")
    BillDto toDto(Bill bill);

    @InheritInverseConfiguration
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "payments", ignore = true)
    Bill toEntity(BillDto dto);
}
