package com.finsight.transaction.repository;

import com.finsight.transaction.model.Transaction;
import com.finsight.transaction.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountId(UUID accountId);
    List<Transaction> findByStatus(TransactionStatus status);
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
