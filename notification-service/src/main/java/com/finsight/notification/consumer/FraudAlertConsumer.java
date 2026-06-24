package com.finsight.notification.consumer;

import com.finsight.notification.model.Notification;
import com.finsight.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAlertConsumer {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "transactions.flagged", groupId = "notification-service-group")
    public void consume(String message) {
        log.info("Notification service received flagged event: {}", message);
        try {
            String transactionId = extractField(message, "transactionId");
            String accountId     = extractField(message, "accountId");
            String riskLevel     = extractField(message, "riskLevel");
            String riskScoreStr  = extractField(message, "riskScore");
            String merchant      = extractField(message, "merchant");
            String amount        = extractField(message, "amount");

            double riskScore = riskScoreStr != null ? Double.parseDouble(riskScoreStr) : 0.0;

            // Build alert message
            String alertMsg = String.format(
                "🚨 FRAUD ALERT: Transaction %s for £%s at %s flagged as %s (risk score: %.2f)",
                transactionId, amount, merchant, riskLevel, riskScore
            );

            log.warn("ALERT: {}", alertMsg);

            // Save notification to database (Outbox Pattern)
            Notification notification = new Notification();
            notification.setAccountId(accountId);
            notification.setTransactionId(transactionId);
            notification.setType("FRAUD_ALERT");
            notification.setMessage(alertMsg);
            notification.setRiskLevel(riskLevel);
            notification.setRiskScore(riskScore);
            notificationRepository.save(notification);

            log.info("Notification saved for transaction: {}", transactionId);

            // In production: send email, SMS, push notification here
            sendAlert(accountId, alertMsg, riskLevel);

        } catch (Exception e) {
            log.error("Error processing fraud alert: {}", e.getMessage());
        }
    }

    private void sendAlert(String accountId, String message, String riskLevel) {
        // TODO: Integrate with email/SMS service in production
        // For now: log the alert
        log.info("📧 NOTIFICATION SENT to account {}: [{}] {}",
                 accountId, riskLevel, message);
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
