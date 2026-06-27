package com.trading.trading_signal.controller;

import com.trading.trading_signal.dto.SignalRequestDTO;
import com.trading.trading_signal.dto.SignalResponseDTO;
import com.trading.trading_signal.service.SignalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/signals")
@Tag(name = "Trading Signals", description = "Create and track trading signals against live Binance prices")
@RequiredArgsConstructor
public class SignalController {
    private final SignalService signalService;

    @PostMapping
    @Operation(summary = "Create a new trading signal")
    public ResponseEntity<SignalResponseDTO> createSignal(@Valid @RequestBody SignalRequestDTO request) {
        SignalResponseDTO created  = signalService.createSignal(request);
        return ResponseEntity.created(URI.create("/api/signals/" + created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "List all trading signals with live status")
    public ResponseEntity<List<SignalResponseDTO>> getAllSignals() {
        return ResponseEntity.ok(signalService.getAllSignals());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single trading signal by id with live status")
    public ResponseEntity<SignalResponseDTO> getSignalById(@PathVariable Long id) {
        SignalResponseDTO signal = signalService.getSignalById(id);
        return ResponseEntity.ok(signal);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a trading signal")
    public ResponseEntity<Void> deleteSignal(@PathVariable Long id) {
        signalService.deleteSignal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Force a fresh status re-evaluation against the live Binance price")
    public ResponseEntity<SignalResponseDTO> refreshStatus(@PathVariable Long id) {
        return ResponseEntity.ok(signalService.refreshStatus(id));
    }
}
