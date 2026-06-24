package com.finsight.analytics.controller;

import com.finsight.analytics.model.SpendingAnalytics;
import com.finsight.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/spending/{accountId}")
    public ResponseEntity<List<SpendingAnalytics>> getSpending(
            @PathVariable String accountId) {
        return ResponseEntity.ok(analyticsService.getSpendingByAccount(accountId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FinSight Analytics Service is running ✅");
    }
}
