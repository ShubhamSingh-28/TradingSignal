package com.trading.trading_signal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingsignalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingsignalApplication.class, args);
	}

}
