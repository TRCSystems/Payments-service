package com.tribesystems.payment.transaction.model;

import com.tribesystems.payment.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "payment")
@Builder
@Data
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(unique = true, nullable = false)
    private String paymentOriginatorConversationID;
    @Column
    private String paymentInitiatorName;
    @Column
    private String paymentAmount;
    @Column
    private String paymentPartyA;
    @Column
    private String paymentPartyB;
    @Column
    private String paymentRemarks;
    @Column
    private String paymentOccassion;

    @Column(nullable = false)
    private String paymentStatus;
}
