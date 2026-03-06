package com.tribesystems.payment.mpesa.dto;

public record InitiateB2BTransactionDto(
        String shortCode,
        double amount,
        String paymentReference
) {
}
