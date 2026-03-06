package com.tribesystems.payment.transaction.mapper;

import com.tribesystems.payment.mpesa.dto.*;
import com.tribesystems.payment.transaction.enums.TransactionType;
import com.tribesystems.payment.transaction.model.B2BTransaction;
import com.tribesystems.payment.transaction.model.ConfirmedTransaction;
import com.tribesystems.payment.transaction.model.Payment;
import com.tribesystems.payment.transaction.model.Transaction;
import jakarta.persistence.Column;

public class TransactionMapper {

    public static Transaction initiatePaymentResponseToTransactionMapper(InitiatePaymentResponse resp, InitiatePaymentDto dto, String status)
    {
        return Transaction.builder()
                .MerchantRequestID(resp.MerchantRequestID())
                .CheckoutRequestID(resp.CheckoutRequestID())
                .transactionStatus(status)
                .transactionType(TransactionType.MPESA)
                .ResponseCode(resp.ResponseCode())
                .customerPhoneNumber(dto.phoneNumber())
                .amount(dto.amount())
                .build();
    }

    public static ConfirmedTransaction transactionCallbackRequestToConfirmedTransactionMapper(TransactionCallbackRequest request, String transactionStatus)
    {

        return ConfirmedTransaction.builder()
                .mpesaTransactionStatus(transactionStatus)
                .mpesaTransactionType(request.TransactionType())
                .mpesaTransactionId(request.TransID())
                .mpesaTransactionTime(request.TransTime())
                .mpesaTransactionAmount(request.TransAmount())
                .mpesaTransactionBusinessShortCode(request.BusinessShortCode())
                .mpesaTransactionBillRefNumber(request.BillRefNumber())
                .mpesaTransactionInvoiceNumber(request.InvoiceNumber())
                .mpesaTransactionOrgAccountBalance(request.OrgAccountBalance())
                .mpesaTransactionThirdPartyTransId(request.ThirdPartyTransID())
                .mpesaTransactionMSISDN(request.MSISDN())
                .mpesaTransactionFirstName(request.FirstName())
                .mpesaTransactionMiddleName(request.MiddleName())
                .mpesaTransactionLastName(request.LastName())
                .build();
    }

    public static Payment b2cRequestToPaymentMapper(B2CRequest request, String paymentStatus)
    {
        return Payment.builder()
                .paymentOriginatorConversationID(request.OriginatorConversationID())
                .paymentInitiatorName(request.InitiatorName())
                .paymentAmount(request.Amount())
                .paymentPartyA(request.PartyA())
                .paymentPartyB(request.PartyB())
                .paymentRemarks(request.Remarks())
                .paymentOccassion(request.Occassion())
                .paymentStatus(paymentStatus)
                .build();
    }

    public static B2BTransaction b2bTransactionRequestToB2BTransactionMapper(B2BTransactionRequest request, String transactionStatus)
    {
        return B2BTransaction.builder()
            .primaryShortCode(request.primaryShortCode())
            .receiverShortCode(request.receiverShortCode())
            .amount(request.amount() + "")
            .paymentRef(request.paymentRef())
            .partnerName(request.partnerName())
            .RequestRefID(request.RequestRefID())
            .transactionStatus(transactionStatus)
            .build();
    }
}
