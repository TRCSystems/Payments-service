package com.tribesystems.payment.transaction.model;

import com.tribesystems.payment.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "b2b_transaction")
@Builder
@Data
public class B2BTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long b2bTransactionId;

    @Column
    private String primaryShortCode;
    @Column
    private String receiverShortCode;
    @Column
    private String amount;
    @Column
    private String paymentRef;
    @Column
    private String partnerName;
    @Column
    private String RequestRefID;

    @Column
    private String transactionStatus;

    @Column
    private String mpesaTransactionId;
}
