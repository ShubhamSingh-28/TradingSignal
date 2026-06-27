package com.trading.trading_signal.repository;

import com.trading.trading_signal.modal.SignalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trading.trading_signal.modal.Signal;

import java.util.List;

public interface SignalRepository extends JpaRepository<Signal, Long> {
    List<Signal> findByStatus(SignalStatus status);
}
