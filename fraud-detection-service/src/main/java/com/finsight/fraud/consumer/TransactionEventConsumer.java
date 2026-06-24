package com.finsight.fraud.consumer;

import com.finsight.fraud.model.TransactionEvent;
import com.finsight.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(
        topics = "transactions.raw",
        groupId = "fraud-detection-group"
    )
    public void consume(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received message from topic {}: {}", topic, message);
        try {
            TransactionEvent event = parseEvent(message);
            fraudDetectionService.processTransaction(event);
        } catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage());
        }
    }

    // Simple manual JSON parsing — no external dependency needed
    private TransactionEvent parseEvent(String json) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(extractField(json, "transactionId"));
        event.setAccountId(extractField(json, "accountId"));
        event.setMerchant(extractField(json, "merchant"));
        event.setCategory(extractField(json, "category"));
        event.setCurrency(extractField(json, "currency"));
        event.setStatus(extractField(json, "status"));
        String amount = extractField(json, "amount");
        if (amount != null && !amount.isEmpty()) {
            event.setAmount(new BigDecimal(amount));
        }
        return event;
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } else {
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            return json.substring(start, end).trim();
        }
    }
}
