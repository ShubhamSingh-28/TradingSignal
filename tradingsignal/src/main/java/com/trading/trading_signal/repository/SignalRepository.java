package com.trading.trading_signal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trading.trading_signal.modal.Signal;

public interface SignalRepository extends JpaRepository<Signal, Long> {
    
}
