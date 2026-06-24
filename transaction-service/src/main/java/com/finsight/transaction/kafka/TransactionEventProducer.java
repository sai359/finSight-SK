package com.finsight.transaction.kafka;

import com.finsight.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "transactions.raw";

    public void publishTransaction(Transaction transaction) {
        String event = String.format(
            "{\"transactionId\":\"%s\",\"accountId\":\"%s\",\"amount\":%s," +
            "\"currency\":\"%s\",\"merchant\":\"%s\",\"category\":\"%s\",\"status\":\"%s\"}",
            transaction.getId(), transaction.getAccountId(), transaction.getAmount(),
            transaction.getCurrency(), transaction.getMerchant(),
            transaction.getCategory() != null ? transaction.getCategory() : "",
            transaction.getStatus()
        );
        kafkaTemplate.send(TOPIC, transaction.getId().toString(), event);
        log.info("Published to {}: transactionId={}", TOPIC, transaction.getId());
    }
}
