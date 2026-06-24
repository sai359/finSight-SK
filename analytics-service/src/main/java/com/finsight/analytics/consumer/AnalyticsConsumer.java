package com.finsight.analytics.consumer;

import com.finsight.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "transactions.raw", groupId = "analytics-service-group")
    public void consume(String message) {
        log.info("Analytics received: {}", message);
        try {
            String accountId = extractField(message, "accountId");
            String amountStr = extractField(message, "amount");
            String category = extractField(message, "category");

            if (accountId != null && amountStr != null) {
                BigDecimal amount = new BigDecimal(amountStr);
                analyticsService.processTransaction(
                    accountId, amount, category, LocalDate.now()
                );
            }
        } catch (Exception e) {
            log.error("Error processing analytics event: {}", e.getMessage());
        }
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end > 0 ? json.substring(start, end) : null;
        } else {
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            return end > 0 ? json.substring(start, end).trim() : null;
        }
    }
}
