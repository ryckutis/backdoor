package com.example.demo.service;

import com.example.demo.client.FxRates;
import com.example.demo.model.ExchangeRate;
import com.example.demo.repository.ExchangeRateRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class FxRatesService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public FxRates fetchCurrentRates() throws Exception {
        String url = "https://www.lb.lt/webservices/FxRates/FxRates.asmx/getCurrentFxRates";
        String xmlResponse = restTemplate.getForObject(url, String.class);
        System.out.println("RAW response:\n" + xmlResponse);
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.readValue(xmlResponse, FxRates.class);
    }

    public List<ExchangeRate> mapToExchangeRates(FxRates fxRates) {
        List<ExchangeRate> exchangeRates = new ArrayList<>();

        if (fxRates == null || fxRates.getFxRate() == null) {
            return exchangeRates;
        }

        for (FxRates.FxRate fxRate : fxRates.getFxRate()) {
            String baseCurrency = fxRate.getTp();
            if (fxRate.getCcyAmt() == null) continue;

            for (FxRates.CcyAmt ccyAmt : fxRate.getCcyAmt()) {
                if (ccyAmt.getCcy() == null || ccyAmt.getAmt() == null) continue;
                if (ccyAmt.getCcy().equalsIgnoreCase(baseCurrency)) continue;

                ExchangeRate rate = new ExchangeRate();
                rate.setBaseCurrency(baseCurrency);
                rate.setTargetCurrency(ccyAmt.getCcy());
                rate.setRate(ccyAmt.getAmt());
                rate.setDate(fxRate.getDt());

                exchangeRates.add(rate);
            }
        }
        return exchangeRates;
    }

    public void saveCurrentRates() throws Exception {
        FxRates fxRates = fetchCurrentRates();
        List<ExchangeRate> rates = mapToExchangeRates(fxRates);
        exchangeRateRepository.saveAll(rates);
    }
}
