package com.trading.trading_signal.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trading.trading_signal.exception.BinanceApiException;

import lombok.Data;

@Service
public class BinancePriceService {

    private static final Logger log = LoggerFactory.getLogger(BinancePriceService.class);

    private final WebClient webClient;

    public BinancePriceService(@Value("${binance.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }


    public BigDecimal getCurrentPrice(String symbol) {
        try {
            TickerPriceResponse response = webClient.get()
                    .uri("/api/v3/ticker/price?symbol={symbol}", symbol)
                    .retrieve()
                    .bodyToMono(TickerPriceResponse.class)
                    .block();

            if (response == null || response.getPrice() == null) {
                throw new BinanceApiException("Binance returned an empty price for symbol: " + symbol);
            }

            return response.getPrice();
        } catch (WebClientException ex) {
            log.error("Failed to fetch price for symbol {} from Binance", symbol, ex);
            throw new BinanceApiException(
                    "Unable to fetch live price for symbol: " + symbol, ex);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TickerPriceResponse {
        private String symbol;
        private BigDecimal price;
    }
}