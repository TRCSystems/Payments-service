package com.tribesystems.payment.transaction.repository;

import com.tribesystems.payment.transaction.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findPaymentByPaymentOriginatorConversationID(String paymentOriginatorConversationID);
}
