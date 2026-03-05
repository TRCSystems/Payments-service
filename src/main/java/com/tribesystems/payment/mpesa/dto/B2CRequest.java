package com.tribesystems.payment.mpesa.dto;

public record B2CRequest(
        String OriginatorConversationID,
        String InitiatorName,
        String SecurityCredential,
        String CommandID,
        String Amount,
        String PartyA,
        String PartyB,
        String Remarks,
        String QueueTimeOutURL,
        String ResultURL,
        String Occassion
) {
}
