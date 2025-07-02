package com.example.demo.repository;

import com.example.demo.model.CurrencyRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRateHistoryRepository extends JpaRepository<CurrencyRateHistory, Long> {
}
