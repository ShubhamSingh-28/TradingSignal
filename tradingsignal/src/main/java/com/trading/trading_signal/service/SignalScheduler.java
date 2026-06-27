package com.trading.trading_signal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trading.trading_signal.modal.Signal;
import com.trading.trading_signal.modal.SignalStatus;
import com.trading.trading_signal.repository.SignalRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalScheduler {

    private final SignalRepository signalRepository;
    private final BinancePriceService binancePriceService;
    private final SignalStatusEvaluator signalStatusEvaluator;

    // runs every 30 seconds
    @Scheduled(fixedDelay = 30_000)
    public void evaluateOpenSignals() {
        List<Signal> openSignals = signalRepository.findByStatus(SignalStatus.OPEN);

        if (openSignals.isEmpty()) return;

        log.info("Scheduler: evaluating {} open signal(s)", openSignals.size());

        for (Signal signal : openSignals) {
            try {
                BigDecimal currentPrice = binancePriceService.getCurrentPrice(signal.getSymbol());
                signalStatusEvaluator.evaluate(signal, currentPrice, LocalDateTime.now());
                signalRepository.save(signal);
            } catch (Exception e) {
                log.error("Scheduler: failed to evaluate signal id={}", signal.getId(), e);
            }
        }
    }
}