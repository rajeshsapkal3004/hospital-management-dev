package com.hospital_management.mapper;


import com.hospital_management.dtos.PaymentHistoryDto;
import com.hospital_management.models.Payment;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "paymentId", source = "id")
    @Mapping(target = "billId", source = "bill.id")
    @Mapping(target = "billNumber", source = "bill.billNumber")
    @Mapping(target = "statusDisplayName", expression = "java(payment.getStatus().getDisplayName())")
    PaymentHistoryDto toHistoryDto(Payment payment);
}
