package com.example.demo.client;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class FxRatesClient {

    private static final String URL = "https://www.lb.lt/webservices/FxRates/FxRates.asmx/getCurrentFxRates";

    public String fetchRates(String tp) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAcceptCharset(java.util.Collections.singletonList(StandardCharsets.UTF_8));

        String body = "tp=" + tp;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                entity,
                String.class);

        return response.getBody();
    }
}
