package com.example.demo.fast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historical")
@CrossOrigin(origins = "*")
public class HistoricalDataController {

    @Autowired
    private HistoricalDataLoader historicalDataLoader;

    @PostMapping("/load-30-days")
    public ResponseEntity<String> load30DaysData() {
        try {
            historicalDataLoader.loadLast30DaysData();
            return ResponseEntity.ok("Historical data for last 30 days loaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to load historical data: " + e.getMessage());
        }
    }

    @PostMapping("/duplicate-current-rates")
    public ResponseEntity<String> duplicateCurrentRates() {
        try {
            historicalDataLoader.duplicateCurrentRatesForPastDays();
            return ResponseEntity.ok("Current rates duplicated for past 30 days successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to duplicate current rates: " + e.getMessage());
        }
    }
}