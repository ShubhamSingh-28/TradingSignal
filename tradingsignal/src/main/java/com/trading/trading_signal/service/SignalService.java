package com.trading.trading_signal.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.trading.trading_signal.modal.SignalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trading.trading_signal.dto.SignalRequestDTO;
import com.trading.trading_signal.dto.SignalResponseDTO;
import com.trading.trading_signal.exception.SignalNotFoundException;
import com.trading.trading_signal.mapper.SignalMapper;
import com.trading.trading_signal.modal.Signal;
import com.trading.trading_signal.repository.SignalRepository;


@Service
@RequiredArgsConstructor
public class SignalService {

    private final SignalRepository signalRepository;
    private final SignalValidator signalValidator;
    private final SignalMapper signalMapper;
    private final BinancePriceService binancePriceService;
    private final SignalStatusEvaluator signalStatusEvaluator;

    @Transactional
    public SignalResponseDTO createSignal(SignalRequestDTO request) {
        signalValidator.validate(request);

        Signal signal = signalMapper.toEntity(request);
        Signal saved = signalRepository.save(signal);

        return signalMapper.toDTO(saved);
    }

    @Transactional
    public List<SignalResponseDTO> getAllSignals() {
        return signalRepository.findAll().stream()
                .map(this::evaluateAndMap)
                .toList();
    }

    @Transactional
    public SignalResponseDTO getSignalById(Long id) {
        Signal signal = findOrThrow(id);
        return evaluateAndMap(signal);
    }

    @Transactional
    public void deleteSignal(Long id) {
        if (!signalRepository.existsById(id)) {
            throw new SignalNotFoundException(id);
        }
        signalRepository.deleteById(id);
    }


    @Transactional
    public SignalResponseDTO refreshStatus(Long id) {
        Signal signal = findOrThrow(id);
        return evaluateAndMap(signal);
    }



    private SignalResponseDTO evaluateAndMap(Signal signal) {
        if (isTerminal(signal.getStatus())) {          // no-op for closed signals
            return signalMapper.toDTO(signal);
        }
        BigDecimal currentPrice = binancePriceService.getCurrentPrice(signal.getSymbol());
        signalStatusEvaluator.evaluate(signal, currentPrice, LocalDateTime.now());
        signalRepository.save(signal);
        return signalMapper.toDTO(signal);
    }

    private boolean isTerminal(SignalStatus status) {
        return status == SignalStatus.TARGET_HIT
                || status == SignalStatus.STOPLOSS_HIT
                || status == SignalStatus.EXPIRED;
    }

    private Signal findOrThrow(Long id) {
        return signalRepository.findById(id)
                .orElseThrow(() -> new SignalNotFoundException(id));
    }
}