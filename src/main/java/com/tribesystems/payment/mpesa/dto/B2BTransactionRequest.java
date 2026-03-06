package com.tribesystems.payment.mpesa.dto;

public record B2BTransactionRequest(
        String primaryShortCode,
        String receiverShortCode,
        double amount,
        String paymentRef,
        String callbackUrl,
        String partnerName,
        String RequestRefID

) {
}
