package com.finsight.fraud.service;

import com.finsight.fraud.model.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based fraud detector.
 * Used as fallback when ML model is unavailable (Circuit Breaker pattern).
 */
@Component
@Slf4j
public class RuleBasedFraudDetector {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal MEDIUM_VALUE_THRESHOLD = new BigDecimal("1000");

    public double calculateRiskScore(TransactionEvent event) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        // Rule 1: High value transaction
        if (event.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            score += 0.4;
            reasons.add("High value transaction: " + event.getAmount());
        } else if (event.getAmount().compareTo(MEDIUM_VALUE_THRESHOLD) > 0) {
            score += 0.2;
            reasons.add("Medium value transaction: " + event.getAmount());
        }

        // Rule 2: Unusual merchant categories
        if (event.getCategory() != null) {
            String cat = event.getCategory().toLowerCase();
            if (cat.contains("casino") || cat.contains("gambling")) {
                score += 0.3;
                reasons.add("High-risk merchant category: " + event.getCategory());
            }
        }

        // Rule 3: Round number amounts (common in fraud)
        if (event.getAmount().scale() == 0 ||
            event.getAmount().remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) == 0) {
            score += 0.1;
            reasons.add("Suspicious round amount");
        }

        double finalScore = Math.min(score, 1.0);
        log.debug("Rule-based score for transaction {}: {} — reasons: {}",
                  event.getTransactionId(), finalScore, reasons);
        return finalScore;
    }

    public List<String> getReasons(TransactionEvent event) {
        List<String> reasons = new ArrayList<>();
        if (event.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0)
            reasons.add("High value transaction");
        if (event.getCategory() != null &&
            event.getCategory().toLowerCase().contains("casino"))
            reasons.add("High-risk merchant category");
        return reasons;
    }
}
