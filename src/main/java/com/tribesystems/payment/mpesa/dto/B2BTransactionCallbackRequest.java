package com.tribesystems.payment.mpesa.dto;

public record B2BTransactionCallbackRequest(
        String resultCode,
        String resultDesc,
        String requestId,
        String amount,
        String paymentReference,
        String resultType,
        String conversationID,
        String transactionId,
        String status
) {
}
