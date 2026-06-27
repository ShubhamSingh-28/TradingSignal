package com.trading.trading_signal.mapper;

import com.trading.trading_signal.dto.SignalRequestDTO;
import com.trading.trading_signal.dto.SignalResponseDTO;
import com.trading.trading_signal.modal.Signal;
import org.springframework.stereotype.Component;

@Component
public class SignalMapper {
    public Signal toEntity(SignalRequestDTO dto){
        return  Signal.builder()
                .symbol(dto.getSymbol().toUpperCase())
                .direction(dto.getDirection())
                .entryPrice(dto.getEntryPrice())
                .stopLoss(dto.getStopLoss())
                .targetPrice(dto.getTargetPrice())
                .entryTime(dto.getEntryTime())
                .expiryTime(dto.getExpiryTime())
                .build();
    }
    public SignalResponseDTO toDTO(Signal signal){
        return SignalResponseDTO.builder()
                .id(signal.getId())
                .symbol(signal.getSymbol())
                .direction(signal.getDirection())
                .entryPrice(signal.getEntryPrice())
                .stopLoss(signal.getStopLoss())
                .targetPrice(signal.getTargetPrice())
                .entryTime(signal.getEntryTime())
                .expiryTime(signal.getExpiryTime())
                .status(signal.getStatus())
                .realizedRoi(signal.getRealizedRoi())
                .createdAt(signal.getCreatedAt())
                .build();
    }
}
