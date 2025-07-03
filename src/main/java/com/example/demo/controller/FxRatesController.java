package com.example.demo.controller;

import com.example.demo.model.ExchangeRate;
import com.example.demo.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fxrates")
@CrossOrigin(origins = "*")
public class FxRatesController {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @PostMapping("/update")
    public ResponseEntity<String> updateExchangeRates() {
        try {
            exchangeRateService.saveCurrentRates();
            return ResponseEntity.ok("Exchange rates updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to update exchange rates: " + e.getMessage());
        }
    }

    @GetMapping("/date")
    public ResponseEntity<List<ExchangeRate>> getRatesByDate(@RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<ExchangeRate> rates = exchangeRateService.getRatesByDate(localDate);
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<List<ExchangeRate>> getCurrentRates() {
        try {
            List<ExchangeRate> rates = exchangeRateService.getCurrentRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/historical/{baseCurrency}/{targetCurrency}")
    public ResponseEntity<List<ExchangeRate>> getHistoricalRates(
            @PathVariable String baseCurrency,
            @PathVariable String targetCurrency,
            @RequestParam(required = false) Integer days) {
        try {
            List<ExchangeRate> rates = exchangeRateService.getHistoricalRates(baseCurrency, targetCurrency, days != null ? days : 30);
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
