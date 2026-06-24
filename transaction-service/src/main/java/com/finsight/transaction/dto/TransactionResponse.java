package com.finsight.transaction.dto;

import com.finsight.transaction.model.TransactionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID id;
    private UUID accountId;
    private BigDecimal amount;
    private String currency;
    private String merchant;
    private String category;
    private String location;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
