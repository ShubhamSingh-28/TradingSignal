# Trading Signal Tracker

A Spring Boot backend that creates and tracks crypto trading signals against live Binance prices.

---

## Prerequisites
- Java 21+
- Maven
- Docker Desktop (must be running)

---

## Setup & Run

### 1. Clone the repo
```bash
git clone https://github.com/yourusername/tradingsignal.git
cd tradingsignal/tradingsignal
```

### 2. Start PostgreSQL via Docker
```bash
docker-compose up -d
```

Verify the container is running:
```bash
docker ps
```
You should see `trading-signal-db` with status `Up`.

The database `trading_signal_db` is created automatically with:
- Host: `localhost`
- Port: `5432`
- Username: `postgres`
- Password: `postgres`

> No manual DB setup needed. Hibernate auto-creates the `signals` table on first run (`ddl-auto: update`).

### 3. Run the application
```bash
mvnw spring-boot:run        # Windows
./mvnw spring-boot:run      # Mac/Linux
```

App starts at `http://localhost:8080`

### 4. Run unit tests
```bash
mvnw test        # Windows
./mvnw test      # Mac/Linux
```

### 5. Access Swagger UI
`http://localhost:8080/swagger-ui.html`

### Stopping the app
```bash
# Stop PostgreSQL container
docker-compose down

# To wipe the database volume entirely
docker-compose down -v
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/signals` | Create a new signal |
| GET | `/api/signals` | Get all signals with live status |
| GET | `/api/signals/{id}` | Get signal by ID with live status |
| DELETE | `/api/signals/{id}` | Delete a signal |
| GET | `/api/signals/{id}/status` | Force status re-evaluation |

---

## Architecture Overview

### Project Structure

    src/main/java/com/trading/trading_signal/
    ├── controller/
    │   └── SignalController.java       # REST endpoints
    ├── service/
    │   ├── SignalService.java          # Core orchestration layer
    │   ├── SignalValidator.java        # BUY/SELL/time business rules
    │   ├── SignalStatusEvaluator.java  # Status transitions + ROI calculation
    │   ├── SignalScheduler.java        # Background job every 30 seconds
    │   └── BinancePriceService.java   # Binance API via WebClient
    ├── repository/
    │   └── SignalRepository.java      # Spring Data JPA
    ├── modal/
    │   ├── Signal.java                # JPA entity
    │   ├── Direction.java             # BUY / SELL enum
    │   └── SignalStatus.java          # OPEN / TARGET_HIT / STOPLOSS_HIT / EXPIRED
    ├── dto/
    │   ├── SignalRequestDTO.java      # Incoming request with Bean Validation
    │   └── SignalResponseDTO.java     # Outgoing response
    ├── mapper/
    │   └── SignalMapper.java          # DTO ↔ Entity conversion
    └── exception/
        ├── GlobalExceptionHandler.java
        ├── SignalNotFoundException.java
        ├── BinanceApiException.java
        └── ErrorResponse.java