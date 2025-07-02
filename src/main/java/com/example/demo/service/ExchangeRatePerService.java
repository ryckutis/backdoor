package com.example.demo.service;

import com.example.demo.model.CurrencyRateHistory;
import com.example.demo.model.ExchangeRate;
import com.example.demo.repository.CurrencyRateHistoryRepository;
import com.example.demo.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeRatePerService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRateHistoryRepository historyRepository;

    public ExchangeRatePerService(ExchangeRateRepository exchangeRateRepository,
                                  CurrencyRateHistoryRepository historyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void saveCurrentRates(List<ExchangeRate> rates) {
        for (ExchangeRate rate : rates) {
            Optional<ExchangeRate> existing = exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndDate(
                    rate.getBaseCurrency(), rate.getTargetCurrency(), rate.getDate());

            if (existing.isEmpty()) {
                exchangeRateRepository.save(rate);
            } else {
                ExchangeRate ex = existing.get();
                if (ex.getRate().compareTo(rate.getRate()) != 0) {
                    CurrencyRateHistory history = new CurrencyRateHistory(
                            ex.getBaseCurrency(),
                            ex.getTargetCurrency(),
                            ex.getRate(),
                            rate.getRate(),
                            LocalDateTime.now()
                    );
                    historyRepository.save(history);

                    ex.setRate(rate.getRate());
                    exchangeRateRepository.save(ex);
                }
            }
        }
    }

    public List<ExchangeRate> getRatesByDate(LocalDate date) {
        return exchangeRateRepository.findAll().stream()
                .filter(rate -> rate.getDate().equals(date))
                .collect(Collectors.toList());
    }
}
