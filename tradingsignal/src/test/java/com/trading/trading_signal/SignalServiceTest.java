package com.trading.trading_signal;

import com.trading.trading_signal.dto.SignalRequestDTO;
import com.trading.trading_signal.dto.SignalResponseDTO;
import com.trading.trading_signal.exception.SignalNotFoundException;
import com.trading.trading_signal.mapper.SignalMapper;
import com.trading.trading_signal.modal.Direction;
import com.trading.trading_signal.modal.Signal;
import com.trading.trading_signal.modal.SignalStatus;
import com.trading.trading_signal.repository.SignalRepository;
import com.trading.trading_signal.service.BinancePriceService;
import com.trading.trading_signal.service.SignalService;
import com.trading.trading_signal.service.SignalStatusEvaluator;
import com.trading.trading_signal.service.SignalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock SignalRepository signalRepository;
    @Mock SignalValidator signalValidator;
    @Mock SignalMapper signalMapper;
    @Mock BinancePriceService binancePriceService;
    @Mock SignalStatusEvaluator signalStatusEvaluator;

    @InjectMocks
    SignalService signalService;

    private Signal openSignal;

    @BeforeEach
    void setUp() {
        openSignal = Signal.builder()
                .id(1L)
                .symbol("BTCUSDT")
                .direction(Direction.BUY)
                .entryPrice(new BigDecimal("100"))
                .stopLoss(new BigDecimal("90"))
                .targetPrice(new BigDecimal("120"))
                .entryTime(LocalDateTime.now().minusMinutes(5))
                .expiryTime(LocalDateTime.now().plusHours(2))
                .status(SignalStatus.OPEN)
                .build();
    }

    @Test
    void createSignal_savesAndReturnsDTO() {
        SignalRequestDTO req = new SignalRequestDTO();
        when(signalMapper.toEntity(req)).thenReturn(openSignal);
        when(signalRepository.save(openSignal)).thenReturn(openSignal);
        when(signalMapper.toDTO(openSignal)).thenReturn(responseDTO(SignalStatus.OPEN));

        SignalResponseDTO result = signalService.createSignal(req);

        verify(signalValidator).validate(req);
        verify(signalRepository).save(openSignal);
        assertEquals(SignalStatus.OPEN, result.getStatus());
    }

    @Test
    void getSignalById_opensSignal_fetchesPriceAndEvaluates() {
        when(signalRepository.findById(1L)).thenReturn(Optional.of(openSignal));
        when(binancePriceService.getCurrentPrice("BTCUSDT")).thenReturn(new BigDecimal("105"));
        when(signalMapper.toDTO(openSignal)).thenReturn(responseDTO(SignalStatus.OPEN));

        signalService.getSignalById(1L);

        verify(binancePriceService).getCurrentPrice("BTCUSDT");
        verify(signalStatusEvaluator).evaluate(eq(openSignal), any(), any());
        verify(signalRepository).save(openSignal);   // must persist
    }

    @Test
    void getSignalById_terminalSignal_skipsLivePriceFetch() {
        openSignal.setStatus(SignalStatus.TARGET_HIT);
        when(signalRepository.findById(1L)).thenReturn(Optional.of(openSignal));
        when(signalMapper.toDTO(openSignal)).thenReturn(responseDTO(SignalStatus.TARGET_HIT));

        signalService.getSignalById(1L);

        verifyNoInteractions(binancePriceService);   // no unnecessary API call
        verify(signalRepository, never()).save(any());
    }

    @Test
    void getSignalById_notFound_throws() {
        when(signalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SignalNotFoundException.class, () -> signalService.getSignalById(99L));
    }

    @Test
    void getAllSignals_returnsListOfDTOs() {
        when(signalRepository.findAll()).thenReturn(List.of(openSignal));
        when(binancePriceService.getCurrentPrice("BTCUSDT")).thenReturn(new BigDecimal("105"));
        when(signalMapper.toDTO(openSignal)).thenReturn(responseDTO(SignalStatus.OPEN));

        List<SignalResponseDTO> result = signalService.getAllSignals();

        assertEquals(1, result.size());
    }

    @Test
    void deleteSignal_notFound_throws() {
        when(signalRepository.existsById(99L)).thenReturn(false);
        assertThrows(SignalNotFoundException.class, () -> signalService.deleteSignal(99L));
    }

    @Test
    void deleteSignal_found_deletesSuccessfully() {
        when(signalRepository.existsById(1L)).thenReturn(true);
        signalService.deleteSignal(1L);
        verify(signalRepository).deleteById(1L);
    }

    private SignalResponseDTO responseDTO(SignalStatus status) {
        return SignalResponseDTO.builder()
                .id(1L).symbol("BTCUSDT").status(status).build();
    }
}