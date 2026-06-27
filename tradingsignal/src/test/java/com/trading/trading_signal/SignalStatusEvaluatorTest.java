package com.trading.trading_signal;

import com.trading.trading_signal.modal.Direction;
import com.trading.trading_signal.modal.Signal;
import com.trading.trading_signal.modal.SignalStatus;
import com.trading.trading_signal.service.SignalStatusEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SignalStatusEvaluatorTest {

    private SignalStatusEvaluator evaluator;
    private final LocalDateTime FUTURE = LocalDateTime.now().plusHours(2);
    private final LocalDateTime PAST   = LocalDateTime.now().minusHours(1);

    @BeforeEach
    void setUp() { evaluator = new SignalStatusEvaluator(); }

    // ── BUY status ────────────────────────────────────────────────────────────

    @Test
    void buy_targetHit_whenPriceAtOrAboveTarget() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("120"), LocalDateTime.now());
        assertEquals(SignalStatus.TARGET_HIT, s.getStatus());
    }

    @Test
    void buy_targetHit_whenPriceAboveTarget() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("130"), LocalDateTime.now());
        assertEquals(SignalStatus.TARGET_HIT, s.getStatus());
    }

    @Test
    void buy_stopLossHit_whenPriceAtOrBelowStopLoss() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("90"), LocalDateTime.now());
        assertEquals(SignalStatus.STOPLOSS_HIT, s.getStatus());
    }

    @Test
    void buy_stopLossHit_whenPriceBelowStopLoss() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("85"), LocalDateTime.now());
        assertEquals(SignalStatus.STOPLOSS_HIT, s.getStatus());
    }

    @Test
    void buy_remainsOpen_whenPriceBetweenSlAndTp() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("105"), LocalDateTime.now());
        assertEquals(SignalStatus.OPEN, s.getStatus());
    }

    // ── SELL status ───────────────────────────────────────────────────────────

    @Test
    void sell_targetHit_whenPriceAtOrBelowTarget() {
        Signal s = sellSignal("100", "110", "80", FUTURE);
        evaluator.evaluate(s, new BigDecimal("80"), LocalDateTime.now());
        assertEquals(SignalStatus.TARGET_HIT, s.getStatus());
    }

    @Test
    void sell_stopLossHit_whenPriceAtOrAboveStopLoss() {
        Signal s = sellSignal("100", "110", "80", FUTURE);
        evaluator.evaluate(s, new BigDecimal("110"), LocalDateTime.now());
        assertEquals(SignalStatus.STOPLOSS_HIT, s.getStatus());
    }

    // ── EXPIRED ───────────────────────────────────────────────────────────────

    @Test
    void signal_expires_whenPastExpiryAndNoHit() {
        Signal s = buySignal("100", "90", "120", PAST);
        evaluator.evaluate(s, new BigDecimal("105"), LocalDateTime.now());
        assertEquals(SignalStatus.EXPIRED, s.getStatus());
    }

    // ── Terminal states are final ─────────────────────────────────────────────

    @Test
    void targetHit_isTerminal_doesNotChangeToExpired() {
        Signal s = buySignal("100", "90", "120", PAST);
        s.setStatus(SignalStatus.TARGET_HIT);
        evaluator.evaluate(s, new BigDecimal("50"), LocalDateTime.now()); // price crashed
        assertEquals(SignalStatus.TARGET_HIT, s.getStatus());
    }

    @Test
    void stopLossHit_isTerminal_doesNotChange() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        s.setStatus(SignalStatus.STOPLOSS_HIT);
        evaluator.evaluate(s, new BigDecimal("130"), LocalDateTime.now()); // price recovered
        assertEquals(SignalStatus.STOPLOSS_HIT, s.getStatus());
    }

    @Test
    void expired_isTerminal_doesNotChange() {
        Signal s = buySignal("100", "90", "120", PAST);
        s.setStatus(SignalStatus.EXPIRED);
        evaluator.evaluate(s, new BigDecimal("130"), LocalDateTime.now());
        assertEquals(SignalStatus.EXPIRED, s.getStatus());
    }

    // ── ROI ───────────────────────────────────────────────────────────────────

    @Test
    void buy_roiIsPositive_whenTargetHit() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("120"), LocalDateTime.now());
        // ROI = (120 - 100) / 100 * 100 = 20.00
        assertEquals(new BigDecimal("20.00"), s.getRealizedRoi());
    }

    @Test
    void buy_roiIsNegative_whenStopLossHit() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("90"), LocalDateTime.now());
        // ROI = (90 - 100) / 100 * 100 = -10.00
        assertEquals(new BigDecimal("-10.00"), s.getRealizedRoi());
    }

    @Test
    void sell_roiIsPositive_whenTargetHit() {
        Signal s = sellSignal("100", "110", "80", FUTURE);
        evaluator.evaluate(s, new BigDecimal("80"), LocalDateTime.now());
        // ROI = (100 - 80) / 100 * 100 = 20.00
        assertEquals(new BigDecimal("20.00"), s.getRealizedRoi());
    }

    @Test
    void sell_roiIsNegative_whenStopLossHit() {
        Signal s = sellSignal("100", "110", "80", FUTURE);
        evaluator.evaluate(s, new BigDecimal("110"), LocalDateTime.now());
        // ROI = (100 - 110) / 100 * 100 = -10.00
        assertEquals(new BigDecimal("-10.00"), s.getRealizedRoi());
    }

    @Test
    void roiIsNull_whenSignalStaysOpen() {
        Signal s = buySignal("100", "90", "120", FUTURE);
        evaluator.evaluate(s, new BigDecimal("105"), LocalDateTime.now());
        assertNull(s.getRealizedRoi());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Signal buySignal(String entry, String sl, String tp, LocalDateTime expiry) {
        return baseSignal(Direction.BUY, entry, sl, tp, expiry);
    }

    private Signal sellSignal(String entry, String sl, String tp, LocalDateTime expiry) {
        return baseSignal(Direction.SELL, entry, sl, tp, expiry);
    }

    private Signal baseSignal(Direction dir, String entry, String sl, String tp, LocalDateTime expiry) {
        return Signal.builder()
                .symbol("BTCUSDT")
                .direction(dir)
                .entryPrice(new BigDecimal(entry))
                .stopLoss(new BigDecimal(sl))
                .targetPrice(new BigDecimal(tp))
                .entryTime(LocalDateTime.now().minusMinutes(10))
                .expiryTime(expiry)
                .status(SignalStatus.OPEN)
                .build();
    }
}