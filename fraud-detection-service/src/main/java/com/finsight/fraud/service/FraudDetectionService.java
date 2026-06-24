package com.finsight.fraud.service;

import com.finsight.fraud.model.FraudAlert;
import com.finsight.fraud.model.FraudAlertRepository;
import com.finsight.fraud.model.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final RuleBasedFraudDetector ruleBasedDetector;
    private final FraudAlertRepository alertRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${fraud.threshold.flag:0.4}")
    private double flagThreshold;

    @Value("${fraud.threshold.block:0.7}")
    private double blockThreshold;

    private static final String FLAGGED_TOPIC = "transactions.flagged";

    public void processTransaction(TransactionEvent event) {
        log.info("Processing fraud check for transaction: {}", event.getTransactionId());

        // Get risk score — rule-based for now, ML integration next
        double riskScore = ruleBasedDetector.calculateRiskScore(event);
        String riskLevel = determineRiskLevel(riskScore);

        log.info("Transaction {} scored {} — level: {}",
                 event.getTransactionId(), riskScore, riskLevel);

        // Save alert for any non-normal transaction
        if (riskScore >= flagThreshold) {
            saveAlert(event, riskScore, riskLevel);
            publishFlaggedEvent(event, riskScore, riskLevel);
        }
    }

    private String determineRiskLevel(double score) {
        if (score >= blockThreshold) return "BLOCK";
        if (score >= flagThreshold)  return "REVIEW";
        return "NORMAL";
    }

    private void saveAlert(TransactionEvent event, double score, String level) {
        FraudAlert alert = new FraudAlert();
        alert.setTransactionId(event.getTransactionId());
        alert.setAccountId(event.getAccountId());
        alert.setRiskScore(score);
        alert.setRiskLevel(level);
        alert.setReasons(ruleBasedDetector.getReasons(event).toString());
        alert.setModelVersion("rule-based-v1.0");
        alertRepository.save(alert);
        log.info("Fraud alert saved for transaction: {}", event.getTransactionId());
    }

    private void publishFlaggedEvent(TransactionEvent event, double score, String level) {
        String flaggedEvent = String.format(
            "{\"transactionId\":\"%s\",\"accountId\":\"%s\"," +
            "\"riskScore\":%s,\"riskLevel\":\"%s\",\"amount\":%s,\"merchant\":\"%s\"}",
            event.getTransactionId(), event.getAccountId(),
            score, level, event.getAmount(), event.getMerchant()
        );
        kafkaTemplate.send(FLAGGED_TOPIC, event.getTransactionId(), flaggedEvent);
        log.info("Published flagged event to {}: transactionId={}",
                 FLAGGED_TOPIC, event.getTransactionId());
    }
}
