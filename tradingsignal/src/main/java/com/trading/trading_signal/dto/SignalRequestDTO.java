package com.trading.trading_signal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trading.trading_signal.modal.Direction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SignalRequestDTO {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Direction is required")
    private Direction direction;

    @NotNull(message = "Entry price is required")
    @Positive(message = "Entry price must be positive")
    private BigDecimal entryPrice;

    @NotNull(message = "Stop loss is required")
    @Positive(message = "Stop loss must be positive")
    private BigDecimal stopLoss;

    @NotNull(message = "Target price is required")
    @Positive(message = "Target price must be positive")
    private BigDecimal targetPrice;

    @NotNull(message = "Entry time is required")
    private LocalDateTime entryTime;

    @NotNull(message = "Expiry time is required")
    private LocalDateTime expiryTime;
}