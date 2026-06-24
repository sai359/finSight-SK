package com.finsight.analytics.service;

import com.finsight.analytics.model.SpendingAnalytics;
import com.finsight.analytics.repository.SpendingAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final SpendingAnalyticsRepository repository;

    public void processTransaction(String accountId, BigDecimal amount,
                                    String category, LocalDate date) {
        String cat = category != null ? category : "Uncategorised";

        // Find existing record for this account + period + category
        Optional<SpendingAnalytics> existing =
            repository.findByAccountIdAndPeriodAndCategory(accountId, date, cat);

        if (existing.isPresent()) {
            // Update existing aggregate
            SpendingAnalytics analytics = existing.get();
            analytics.setTotalSpent(analytics.getTotalSpent().add(amount));
            analytics.setTxCount(analytics.getTxCount() + 1);
            repository.save(analytics);
            log.info("Updated analytics for account={}, category={}, total={}",
                     accountId, cat, analytics.getTotalSpent());
        } else {
            // Create new aggregate
            SpendingAnalytics analytics = new SpendingAnalytics();
            analytics.setAccountId(accountId);
            analytics.setPeriod(date);
            analytics.setCategory(cat);
            analytics.setTotalSpent(amount);
            analytics.setTxCount(1);
            repository.save(analytics);
            log.info("Created analytics for account={}, category={}", accountId, cat);
        }
    }

    public List<SpendingAnalytics> getSpendingByAccount(String accountId) {
        return repository.findByAccountIdOrderByPeriodDesc(accountId);
    }
}
