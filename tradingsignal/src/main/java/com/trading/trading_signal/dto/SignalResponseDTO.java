package com.trading.trading_signal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trading.trading_signal.modal.Direction;
import com.trading.trading_signal.modal.SignalStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignalResponseDTO {

    private Long id;

    private String symbol;

    private Direction direction;

    private BigDecimal entryPrice;

    private BigDecimal stopLoss;

    private BigDecimal targetPrice;

    private LocalDateTime entryTime;

    private LocalDateTime expiryTime;

    private SignalStatus status;

    private BigDecimal realizedRoi;

    private LocalDateTime createdAt;
}