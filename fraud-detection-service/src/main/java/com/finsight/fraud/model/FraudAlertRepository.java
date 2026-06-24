package com.finsight.fraud.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {
    List<FraudAlert> findByAccountId(String accountId);
    List<FraudAlert> findByReviewedFalse();
    List<FraudAlert> findByRiskLevel(String riskLevel);
}
