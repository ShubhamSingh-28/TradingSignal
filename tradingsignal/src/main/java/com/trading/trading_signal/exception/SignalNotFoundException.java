package com.trading.trading_signal.exception;

public class SignalNotFoundException  extends RuntimeException {
    public SignalNotFoundException(long id) {
        super("Signal with id " + id + " not found");
    }
}
