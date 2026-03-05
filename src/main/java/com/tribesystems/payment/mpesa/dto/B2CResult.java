package com.tribesystems.payment.mpesa.dto;

public record B2CResult(
        String ResultType,
        String ResultCode,
        String ResultDesc,
        String OriginatorConversationID,
        String ConversationID,
        String TransactionID,
        ResultParameters ResultParameters,
        B2CReferenceData ReferenceData
) {
}
