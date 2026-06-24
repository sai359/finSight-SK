package com.finsight.fraud.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionEvent {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String merchant;
    private String category;
    private String status;
}
