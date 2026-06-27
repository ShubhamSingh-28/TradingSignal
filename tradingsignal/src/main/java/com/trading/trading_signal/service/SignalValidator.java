package com.trading.trading_signal.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.trading.trading_signal.dto.SignalRequestDTO;
import com.trading.trading_signal.modal.Direction;

@Service
public class SignalValidator {

    public void validate(SignalRequestDTO dto) {

        validateTime(dto);

        if (dto.getDirection() == Direction.BUY) {
            validateBuy(dto);
        } else {
            validateSell(dto);
        }
    }

    private void validateBuy(SignalRequestDTO dto) {

        if (dto.getStopLoss().compareTo(dto.getEntryPrice()) >= 0) {
            throw new IllegalArgumentException(
                    "For BUY signal, stop loss must be less than entry price.");
        }

        if (dto.getTargetPrice().compareTo(dto.getEntryPrice()) <= 0) {
            throw new IllegalArgumentException(
                    "For BUY signal, target price must be greater than entry price.");
        }
    }

    private void validateSell(SignalRequestDTO dto) {

        if (dto.getStopLoss().compareTo(dto.getEntryPrice()) <= 0) {
            throw new IllegalArgumentException(
                    "For SELL signal, stop loss must be greater than entry price.");
        }

        if (dto.getTargetPrice().compareTo(dto.getEntryPrice()) >= 0) {
            throw new IllegalArgumentException(
                    "For SELL signal, target price must be less than entry price.");
        }
    }

    private void validateTime(SignalRequestDTO dto) {

        if (dto.getExpiryTime().isBefore(dto.getEntryTime())) {
            throw new IllegalArgumentException(
                    "Expiry time must be after entry time.");
        }

        if (dto.getEntryTime().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new IllegalArgumentException(
                    "Entry time cannot be older than 24 hours.");
        }
    }
}