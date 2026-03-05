package com.tribesystems.payment.mpesa.dto;

public record B2CResponse(
        String ConversationID,
        String OriginatorConversationID,
        String ResponseCode,
        String ResponseDescription
) {
}
