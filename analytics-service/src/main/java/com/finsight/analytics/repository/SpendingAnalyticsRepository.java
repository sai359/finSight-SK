package com.finsight.analytics.repository;

import com.finsight.analytics.model.SpendingAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

@Repository
public interface SpendingAnalyticsRepository
        extends JpaRepository<SpendingAnalytics, UUID> {

    List<SpendingAnalytics> findByAccountId(String accountId);

    Optional<SpendingAnalytics> findByAccountIdAndPeriodAndCategory(
        String accountId, LocalDate period, String category);

    @Query("SELECT s FROM SpendingAnalytics s WHERE s.accountId = :accountId ORDER BY s.period DESC")
    List<SpendingAnalytics> findByAccountIdOrderByPeriodDesc(String accountId);
}
