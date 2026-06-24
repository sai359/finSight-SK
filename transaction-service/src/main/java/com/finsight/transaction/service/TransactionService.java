package com.finsight.transaction.service;

import com.finsight.transaction.dto.TransactionRequest;
import com.finsight.transaction.dto.TransactionResponse;
import com.finsight.transaction.exception.TransactionNotFoundException;
import com.finsight.transaction.kafka.TransactionEventProducer;
import com.finsight.transaction.model.Transaction;
import com.finsight.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionEventProducer eventProducer;

    public TransactionResponse createTransaction(TransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setAccountId(request.getAccountId());
        tx.setAmount(request.getAmount());
        tx.setMerchant(request.getMerchant());
        tx.setCurrency(request.getCurrency());
        tx.setCategory(request.getCategory());
        tx.setLocation(request.getLocation());

        Transaction saved = repository.save(tx);
        log.info("Transaction created: id={}, amount={}", saved.getId(), saved.getAmount());
        eventProducer.publishTransaction(saved);
        return mapToResponse(saved);
    }

    public TransactionResponse getTransaction(UUID id) {
        return mapToResponse(repository.findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id.toString())));
    }

    public List<TransactionResponse> getByAccount(UUID accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId)
            .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions() {
        return repository.findAll().stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setAccountId(tx.getAccountId());
        r.setAmount(tx.getAmount());
        r.setCurrency(tx.getCurrency());
        r.setMerchant(tx.getMerchant());
        r.setCategory(tx.getCategory());
        r.setLocation(tx.getLocation());
        r.setStatus(tx.getStatus());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }
}
