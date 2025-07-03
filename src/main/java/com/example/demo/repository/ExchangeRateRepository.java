package com.example.demo.repository;

import com.example.demo.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndDate(String baseCurrency, String targetCurrency, LocalDate date);

    List<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndDateBetween(String baseCurrency, String targetCurrency, LocalDate startDate, LocalDate endDate);
}
