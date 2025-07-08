package com.example.demo.fast;

import com.example.demo.client.FxRates;
import com.example.demo.model.ExchangeRate;
import com.example.demo.service.ExchangeRatePerService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoricalDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalDataLoader.class);
    private static final String LB_HISTORICAL_URL = "https://www.lb.lt/webservices/FxRates/FxRates.asmx/getFxRates";
    private static final String LB_CURRENT_URL = "https://www.lb.lt/webservices/FxRates/FxRates.asmx/getCurrentFxRates";
    private final ExchangeRatePerService exchangeRatePerService;
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;

    public HistoricalDataLoader(ExchangeRatePerService exchangeRatePerService) {
        this.exchangeRatePerService = exchangeRatePerService;
        this.restTemplate = new RestTemplate();
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.registerModule(new JavaTimeModule());
    }

    public void loadLast30DaysData() {
        logger.info("Starting historical data loading for last 30 days");

        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(29);

        logger.info("Loading data from {} to {}", startDate, endDate);

        List<String> types = List.of("LT", "EU");

        for (String tp : types) {
            logger.info("Loading historical data for type: {}", tp);
            loadHistoricalDataForType(startDate, endDate, tp);
        }

        logger.info("Historical data loading completed");
    }


    private void loadHistoricalDataForType(LocalDate startDate, LocalDate endDate, String tp) {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            try {
                boolean success = loadDataForSingleDay(currentDate, tp);
                if (success) {
                    successCount++;
                } else {
                    skipCount++;
                }

                Thread.sleep(100);

            } catch (Exception e) {
                failCount++;
                logger.warn("Failed to load data for {} (type: {}): {}",
                        currentDate, tp, e.getMessage());
            }

            currentDate = currentDate.plusDays(1);
        }

        logger.info("Type {} loading completed - Success: {}, Failed: {}, Skipped: {}",
                tp, successCount, failCount, skipCount);
    }


    private boolean loadDataForSingleDay(LocalDate date, String tp) {
        try {
            String xmlData = fetchHistoricalDataForDate(date, tp);

            if (xmlData != null && !xmlData.trim().isEmpty()) {
                return processXmlData(xmlData, tp, date);
            }

            logger.debug("No data returned for {} (type: {})", date, tp);
            return false;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                logger.debug("No data available for {} (type: {}) - likely weekend/holiday", date, tp);
                return false;
            } else {
                logger.error("HTTP error loading data for {} (type: {}): {} - {}",
                        date, tp, e.getStatusCode(), e.getResponseBodyAsString());
                throw e;
            }
        } catch (Exception e) {
            logger.error("Error loading data for {} (type: {}): {}", date, tp, e.getMessage());
            throw e;
        }
    }


    private String fetchHistoricalDataForDate(LocalDate date, String tp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                String dateStr = date.format(formatter);
                String body = "tp=" + tp + "&dt=" + dateStr;

                HttpEntity<String> entity = new HttpEntity<>(body, headers);

                logger.debug("Trying to fetch data with URL: {}, Body: {}", LB_HISTORICAL_URL, body);

                ResponseEntity<String> response = restTemplate.exchange(
                        LB_HISTORICAL_URL,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() &&
                        response.getBody() != null &&
                        !response.getBody().trim().isEmpty()) {

                    logger.debug("Successfully fetched data for {} (type: {}) using format: {}",
                            date, tp, formatter);
                    return response.getBody();
                }

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404) {
                    continue;
                } else {
                    throw e;
                }
            } catch (Exception e) {
                logger.debug("Format {} failed for date {}: {}", formatter, date, e.getMessage());
                continue;
            }
        }

        return null;
    }


    private boolean processXmlData(String xmlData, String tp, LocalDate date) {
        try {
            xmlData = cleanXmlData(xmlData);

            logger.debug("Processing XML data for {} (type: {})", date, tp);
            logger.debug("XML Content (first 200 chars): {}",
                    xmlData.length() > 200 ? xmlData.substring(0, 200) + "..." : xmlData);

            FxRates fxRates = xmlMapper.readValue(xmlData, FxRates.class);

            List<ExchangeRate> rates = convertToExchangeRates(fxRates);

            if (!rates.isEmpty()) {
                exchangeRatePerService.saveCurrentRates(rates);
                logger.info("Saved {} rates for {} (type: {})", rates.size(), date, tp);
                return true;
            } else {
                logger.debug("No valid rates found in XML for {} (type: {})", date, tp);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error processing XML data for {} (type: {}): {}", date, tp, e.getMessage());
            logger.debug("Problematic XML: {}", xmlData);
            return false;
        }
    }


    private String cleanXmlData(String xmlData) {
        if (xmlData == null) return null;

        if (xmlData.startsWith("\uFEFF")) {
            xmlData = xmlData.substring(1);
        }

        xmlData = xmlData.trim();

        if (!xmlData.startsWith("<?xml") && !xmlData.startsWith("<")) {
            logger.warn("XML data doesn't start with proper XML declaration");
        }

        return xmlData;
    }


    private List<ExchangeRate> convertToExchangeRates(FxRates fxRates) {
        List<ExchangeRate> rates = new ArrayList<>();

        if (fxRates == null || fxRates.getFxRate() == null) {
            logger.debug("FxRates or FxRate list is null");
            return rates;
        }

        for (FxRates.FxRate fxRate : fxRates.getFxRate()) {
            if (fxRate.getCcyAmt() != null) {
                for (FxRates.CcyAmt ccyAmt : fxRate.getCcyAmt()) {
                    if (!ccyAmt.getCcy().equalsIgnoreCase(fxRate.getTp())) {
                        ExchangeRate exchangeRate = new ExchangeRate();
                        exchangeRate.setBaseCurrency(fxRate.getTp());
                        exchangeRate.setTargetCurrency(ccyAmt.getCcy());
                        exchangeRate.setRate(ccyAmt.getAmt());
                        exchangeRate.setDate(fxRate.getDt());
                        rates.add(exchangeRate);
                    }
                }
            }
        }

        logger.debug("Converted {} FxRates to {} ExchangeRate entities",
                fxRates.getFxRate().size(), rates.size());
        return rates;
    }


    public void duplicateCurrentRatesForPastDays() {
        logger.info("Using fallback method - duplicating current rates for past 30 days");

        try {
            List<ExchangeRate> currentRates = getCurrentRatesFromAPI();

            if (currentRates.isEmpty()) {
                logger.error("No current rates available to duplicate");
                return;
            }

            List<ExchangeRate> allRates = new ArrayList<>();
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(29);

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                for (ExchangeRate rate : currentRates) {
                    ExchangeRate newRate = new ExchangeRate();
                    newRate.setBaseCurrency(rate.getBaseCurrency());
                    newRate.setTargetCurrency(rate.getTargetCurrency());
                    newRate.setRate(rate.getRate());
                    newRate.setDate(currentDate);
                    allRates.add(newRate);
                }
                currentDate = currentDate.plusDays(1);
            }

            exchangeRatePerService.saveCurrentRates(allRates);
            logger.info("Duplicated current rates for past 30 days. Total records: {}", allRates.size());

        } catch (Exception e) {
            logger.error("Error duplicating current rates: {}", e.getMessage(), e);
        }
    }


    private List<ExchangeRate> getCurrentRatesFromAPI() {
        List<ExchangeRate> allRates = new ArrayList<>();
        List<String> types = List.of("LT", "EU");

        for (String tp : types) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

                String body = "tp=" + tp;
                HttpEntity<String> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        LB_CURRENT_URL,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String xmlData = cleanXmlData(response.getBody());
                    FxRates fxRates = xmlMapper.readValue(xmlData, FxRates.class);
                    List<ExchangeRate> rates = convertToExchangeRates(fxRates);
                    allRates.addAll(rates);
                    logger.info("Fetched {} current rates for type: {}", rates.size(), tp);
                }
            } catch (Exception e) {
                logger.error("Error fetching current rates for type {}: {}", tp, e.getMessage());
            }
        }

        return allRates;
    }


    public void testAPIConnectivity() {
        logger.info("Testing API connectivity...");

        try {
            List<ExchangeRate> currentRates = getCurrentRatesFromAPI();
            if (!currentRates.isEmpty()) {
                logger.info("API connectivity test PASSED - fetched {} current rates", currentRates.size());
            } else {
                logger.warn("API connectivity test FAILED - no rates returned");
            }
        } catch (Exception e) {
            logger.error("API connectivity test FAILED with error: {}", e.getMessage(), e);
        }
    }
}