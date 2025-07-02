package com.example.demo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class CurrencyRateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal oldRate;
    private BigDecimal newRate;
    private LocalDateTime changedAt;

    public CurrencyRateHistory() {}

    public CurrencyRateHistory(String baseCurrency, String targetCurrency, BigDecimal oldRate, BigDecimal newRate, LocalDateTime changedAt) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.oldRate = oldRate;
        this.newRate = newRate;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getOldRate() {
        return oldRate;
    }

    public void setOldRate(BigDecimal oldRate) {
        this.oldRate = oldRate;
    }

    public BigDecimal getNewRate() {
        return newRate;
    }

    public void setNewRate(BigDecimal newRate) {
        this.newRate = newRate;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
