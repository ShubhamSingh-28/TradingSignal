## Prerequisites
- Java 21+
- Maven
- Docker Desktop (must be running)

## Setup & Run

### 1. Clone the repo
```bash
git clone https://github.com/yourusername/tradingsignal.git
cd tradingsignal
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
cd tradingsignal
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

## Stopping the app
```bash
# Stop PostgreSQL container
docker-compose down

# To wipe the database volume entirely
docker-compose down -v
```
