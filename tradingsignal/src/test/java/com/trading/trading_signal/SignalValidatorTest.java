package com.trading.trading_signal;

import com.trading.trading_signal.dto.SignalRequestDTO;
import com.trading.trading_signal.modal.Direction;
import com.trading.trading_signal.service.SignalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SignalValidatorTest {

    private SignalValidator validator;

    @BeforeEach
    void setUp() { validator = new SignalValidator(); }

    // ── BUY ──────────────────────────────────────────────────────────────────

    @Test
    void buy_valid_passes() {
        assertDoesNotThrow(() -> validator.validate(buySignal("100", "90", "120")));
    }

    @Test
    void buy_stopLossAboveEntry_throws() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(buySignal("100", "110", "120")));
        assertTrue(ex.getMessage().contains("stop loss must be less than entry"));
    }

    @Test
    void buy_stopLossEqualToEntry_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(buySignal("100", "100", "120")));
    }

    @Test
    void buy_targetBelowEntry_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(buySignal("100", "90", "80")));
    }

    @Test
    void buy_targetEqualToEntry_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(buySignal("100", "90", "100")));
    }

    // ── SELL ─────────────────────────────────────────────────────────────────

    @Test
    void sell_valid_passes() {
        assertDoesNotThrow(() -> validator.validate(sellSignal("100", "110", "80")));
    }

    @Test
    void sell_stopLossBelowEntry_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(sellSignal("100", "90", "80")));
    }

    @Test
    void sell_targetAboveEntry_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(sellSignal("100", "110", "120")));
    }

    // ── Time ─────────────────────────────────────────────────────────────────

    @Test
    void expiryBeforeEntry_throws() {
        SignalRequestDTO dto = buySignal("100", "90", "120");
        dto.setEntryTime(LocalDateTime.now());
        dto.setExpiryTime(LocalDateTime.now().minusHours(1));   // expiry BEFORE entry
        assertThrows(IllegalArgumentException.class, () -> validator.validate(dto));
    }

    @Test
    void entryOlderThan24h_throws() {
        SignalRequestDTO dto = buySignal("100", "90", "120");
        dto.setEntryTime(LocalDateTime.now().minusHours(25));
        dto.setExpiryTime(LocalDateTime.now().plusHours(1));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(dto));
    }

    @Test
    void entryExactly24hAgo_passes() {
        SignalRequestDTO dto = buySignal("100", "90", "120");
        // just inside the 24-hour window — should not throw
        dto.setEntryTime(LocalDateTime.now().minusHours(23).minusMinutes(59));
        dto.setExpiryTime(LocalDateTime.now().plusHours(1));
        assertDoesNotThrow(() -> validator.validate(dto));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private SignalRequestDTO buySignal(String entry, String sl, String tp) {
        return signal(Direction.BUY, entry, sl, tp);
    }

    private SignalRequestDTO sellSignal(String entry, String sl, String tp) {
        return signal(Direction.SELL, entry, sl, tp);
    }

    private SignalRequestDTO signal(Direction dir, String entry, String sl, String tp) {
        SignalRequestDTO dto = new SignalRequestDTO();
        dto.setDirection(dir);
        dto.setEntryPrice(new BigDecimal(entry));
        dto.setStopLoss(new BigDecimal(sl));
        dto.setTargetPrice(new BigDecimal(tp));
        dto.setSymbol("BTCUSDT");
        dto.setEntryTime(LocalDateTime.now().minusMinutes(5));
        dto.setExpiryTime(LocalDateTime.now().plusHours(2));
        return dto;
    }
}