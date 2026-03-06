package com.tribesystems.payment.mpesa.dto;

public record B2BTransactionResponse(
        String code,
        String status,

        String resultCode,
        String resultDescription,
        double amount,
        String transactionId,
        String requestId
) {
}
