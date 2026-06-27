package com.trading.trading_signal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.trading.trading_signal.modal.Direction;
import com.trading.trading_signal.modal.Signal;
import com.trading.trading_signal.modal.SignalStatus;
import org.springframework.stereotype.Service;


@Service
public class SignalStatusEvaluator {

    private static final int ROI_SCALE = 2;

    public void evaluate(Signal signal, BigDecimal currentPrice, LocalDateTime now) {
        if (isTerminal(signal.getStatus())) {
            return;
        }

        SignalStatus newStatus = resolveStatus(signal, currentPrice, now);
        signal.setStatus(newStatus);

        if (newStatus != SignalStatus.OPEN) {
            signal.setRealizedRoi(calculateRoi(signal, currentPrice));
        }
    }

    private SignalStatus resolveStatus(Signal signal, BigDecimal currentPrice, LocalDateTime now) {
        boolean targetHit = isTargetHit(signal, currentPrice);
        boolean stopLossHit = isStopLossHit(signal, currentPrice);

        if (targetHit) {
            return SignalStatus.TARGET_HIT;
        }
        if (stopLossHit) {
            return SignalStatus.STOPLOSS_HIT;
        }
        if (now.isAfter(signal.getExpiryTime())) {
            return SignalStatus.EXPIRED;
        }
        return SignalStatus.OPEN;
    }

    private boolean isTargetHit(Signal signal, BigDecimal currentPrice) {
        if (signal.getDirection() == Direction.BUY) {
            return currentPrice.compareTo(signal.getTargetPrice()) >= 0;
        }
        return currentPrice.compareTo(signal.getTargetPrice()) <= 0;
    }

    private boolean isStopLossHit(Signal signal, BigDecimal currentPrice) {
        if (signal.getDirection() == Direction.BUY) {
            return currentPrice.compareTo(signal.getStopLoss()) <= 0;
        }
        return currentPrice.compareTo(signal.getStopLoss()) >= 0;
    }

    public BigDecimal calculateRoi(Signal signal, BigDecimal currentPrice) {
        BigDecimal entryPrice = signal.getEntryPrice();
        BigDecimal diff = signal.getDirection() == Direction.BUY
                ? currentPrice.subtract(entryPrice)
                : entryPrice.subtract(currentPrice);

        return diff
                .divide(entryPrice, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(ROI_SCALE, RoundingMode.HALF_UP);
    }

    private boolean isTerminal(SignalStatus status) {
        return status == SignalStatus.TARGET_HIT
                || status == SignalStatus.STOPLOSS_HIT
                || status == SignalStatus.EXPIRED;
    }
}