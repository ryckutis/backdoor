package com.example.demo.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rate",
        uniqueConstraints = @UniqueConstraint(columnNames = {"base_currency", "target_currency", "date"}))
public class ExchangeRate {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "base_currency", nullable = false)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDate date;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}

