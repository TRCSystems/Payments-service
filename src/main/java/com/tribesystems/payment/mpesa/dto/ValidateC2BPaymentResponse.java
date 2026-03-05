package com.tribesystems.payment.mpesa.dto;

public record ValidateC2BPaymentResponse(
        String ResultCode,
        String ResultDesc
) {
}
