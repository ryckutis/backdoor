package com.example.demo.service;

import com.example.demo.client.FxRates;
import com.example.demo.client.FxRatesClient;
import com.example.demo.model.ExchangeRate;
import com.example.demo.repository.ExchangeRateRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class ExchangeRateService {

    private final FxRatesClient fxRatesClient;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRatePerService exchangeRatePerService;
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);


    public ExchangeRateService(FxRatesClient fxRatesClient, ExchangeRateRepository exchangeRateRepository, ExchangeRatePerService exchangeRatePerService) {
        this.fxRatesClient = fxRatesClient;
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRatePerService = exchangeRatePerService;
    }

    public FxRates fetchAndParseRates(String tp) throws Exception {
        String xml = fxRatesClient.fetchRates(tp);
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        return xmlMapper.readValue(xml, FxRates.class);
    }

    public void saveCurrentRates() throws Exception {
        List<String> types = List.of("LT", "EU");

        for (String tp : types) {
            FxRates fxRates = fetchAndParseRates(tp);

            List<ExchangeRate> rates = fxRates.getFxRate().stream()
                    .flatMap(fxRate -> fxRate.getCcyAmt().stream()
                            .filter(ca -> !ca.getCcy().equalsIgnoreCase(fxRate.getTp()))
                            .map(ca -> {
                                ExchangeRate er = new ExchangeRate();
                                er.setBaseCurrency(fxRate.getTp());
                                er.setTargetCurrency(ca.getCcy());
                                er.setRate(ca.getAmt());
                                er.setDate(fxRate.getDt());
                                return er;
                            }))
                    .toList();

            exchangeRatePerService.saveCurrentRates(rates);
        }
    }

    public List<ExchangeRate> getRatesByDate(LocalDate date) {
        return exchangeRatePerService.getRatesByDate(date);
    }

    public List<ExchangeRate> getCurrentRates() {
        LocalDate today = LocalDate.now();
        return exchangeRatePerService.getRatesByDate(today);
    }

    public List<ExchangeRate> getHistoricalRates(String baseCurrency, String targetCurrency, int days) {
        Optional<LocalDate> mostRecentDate = exchangeRateRepository.findMostRecentDateForCurrencyPair(baseCurrency, targetCurrency);

        LocalDate endDate;
        if (mostRecentDate.isPresent()) {
            endDate = mostRecentDate.get();
        } else {
            endDate = LocalDate.now();
            logger.warn("No historical data found for {}/{}, using current date as fallback", baseCurrency, targetCurrency);
        }

        LocalDate startDate = endDate.minusDays(days - 1);

        logger.info("Fetching historical rates for {}/{} from {} to {} ({} days)",
                baseCurrency, targetCurrency, startDate, endDate, days);

        List<ExchangeRate> rates = exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndDateBetween(
                baseCurrency, targetCurrency, startDate, endDate);

        logger.info("Found {} historical rates", rates.size());
        return rates;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledDailyRateUpdate() {
        logger.info("Scheduled job started at {}", LocalDateTime.now());
        try {
            saveCurrentRates();
            logger.info("Exchange rates updated by scheduled job.");
        } catch (Exception e) {
            logger.error("Failed to update exchange rates during scheduled task", e);
        }
    }
}