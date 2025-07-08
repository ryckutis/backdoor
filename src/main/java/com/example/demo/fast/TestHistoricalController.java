package com.example.demo.fast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestHistoricalController {

    private static final Logger logger = LoggerFactory.getLogger(TestHistoricalController.class);

    @Autowired
    private HistoricalDataLoader historicalDataLoader;

    @PostMapping("/load-historical-data")
    public ResponseEntity<Map<String, Object>> loadHistoricalData() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("=== Starting historical data load ===");

            historicalDataLoader.testAPIConnectivity();

            historicalDataLoader.loadLast30DaysData();

            response.put("success", true);
            response.put("message", "Historical data loading completed successfully");
            response.put("instructions", "Check the application logs for detailed progress information");

            logger.info("=== Historical data load completed ===");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Historical data loading failed", e);

            response.put("success", false);
            response.put("message", "Failed to load historical data: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/test-api-connectivity")
    public ResponseEntity<Map<String, Object>> testAPIConnectivity() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("=== Testing API connectivity ===");

            historicalDataLoader.testAPIConnectivity();

            response.put("success", true);
            response.put("message", "API connectivity test completed");
            response.put("instructions", "Check the application logs for results");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("API connectivity test failed", e);

            response.put("success", false);
            response.put("message", "API connectivity test failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/duplicate-current-rates")
    public ResponseEntity<Map<String, Object>> duplicateCurrentRates() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("=== Starting current rates duplication ===");

            historicalDataLoader.duplicateCurrentRatesForPastDays();

            response.put("success", true);
            response.put("message", "Current rates duplicated for past 30 days successfully");
            response.put("note", "This is a fallback method when historical data is not available");

            logger.info("=== Current rates duplication completed ===");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Current rates duplication failed", e);

            response.put("success", false);
            response.put("message", "Failed to duplicate current rates: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/load-historical-data-custom")
    public ResponseEntity<Map<String, Object>> loadHistoricalDataCustom(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            logger.info("=== Loading custom date range: {} to {} ===", start, end);

            response.put("success", false);
            response.put("message", "Custom date range loading not yet implemented");
            response.put("note", "You can implement this by calling the loadHistoricalDataForType method with custom dates");
            response.put("startDate", start.toString());
            response.put("endDate", end.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Custom date range loading failed", e);

            response.put("success", false);
            response.put("message", "Failed to load custom date range: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();

        response.put("service", "Historical Data Loader");
        response.put("status", "operational");
        response.put("endpoints", Map.of(
                "loadHistoricalData", "POST /api/test/load-historical-data",
                "testAPIConnectivity", "POST /api/test/test-api-connectivity",
                "duplicateCurrentRates", "POST /api/test/duplicate-current-rates",
                "loadCustomDateRange", "POST /api/test/load-historical-data-custom?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD"
        ));
        response.put("instructions", "Use the endpoints above to test and load historical data");

        return ResponseEntity.ok(response);
    }
}