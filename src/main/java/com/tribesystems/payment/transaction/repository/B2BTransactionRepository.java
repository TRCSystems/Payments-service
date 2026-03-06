package com.tribesystems.payment.transaction.repository;

import com.tribesystems.payment.transaction.model.B2BTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface B2BTransactionRepository extends JpaRepository<B2BTransaction, Long> {

    Optional<B2BTransaction> findB2BTransactionByRequestRefID(String requestRefId);
}
