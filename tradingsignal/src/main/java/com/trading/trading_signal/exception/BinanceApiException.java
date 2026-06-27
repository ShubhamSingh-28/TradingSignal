package com.trading.trading_signal.exception;

/**
 * Thrown when the Binance public API cannot be reached or returns
 * an unexpected/invalid response for a requested symbol.
 */
public class BinanceApiException extends RuntimeException {

    public BinanceApiException(String message) {
        super(message);
    }

    public BinanceApiException(String message, Throwable cause) {
        super(message, cause);
    }
}