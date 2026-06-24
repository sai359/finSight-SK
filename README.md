# FinSight — Real-Time Financial Transaction Processing Platform

A production-grade distributed system for real-time transaction processing and fraud detection, built with Java, Spring Boot, Apache Kafka, and PostgreSQL.

## Architecture

```
Client Request
      │
      ▼
┌─────────────────────┐
│  Transaction Service │  ← REST API (port 8080)
│  (Producer)          │
└──────────┬──────────┘
           │ publishes to
           ▼
    [Kafka: transactions.raw]
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌────────────┐  ┌──────────────────┐
│  Fraud     │  │  Analytics       │
│  Detection │  │  Service (8082)  │
│  (8081)    │  └──────────────────┘
└─────┬──────┘
      │ publishes to
      ▼
[Kafka: transactions.flagged]
      │
      ▼
┌─────────────────────┐     ┌──────────────────┐
│  Notification       │────▶│  ML Model        │
│  Service (8083)     │     │  Python (8090)   │
└─────────────────────┘     └──────────────────┘
```

## Services

| Service | Port | Responsibility |
|---------|------|----------------|
| `transaction-service` | 8080 | REST API, input validation, Kafka producer |
| `fraud-detection-service` | 8081 | Kafka consumer, rule-based + ML fraud scoring |
| `analytics-service` | 8082 | Spending aggregation, CQRS read model |
| `notification-service` | 8083 | Alert consumer, outbox pattern |
| `ml-model` | 8090 | Python Isolation Forest anomaly scoring |

## Tech Stack

- **Java 17** + Spring Boot 3.2
- **Apache Kafka** — async inter-service messaging
- **PostgreSQL 16** — persistent storage per service
- **Python 3.11** + Flask + scikit-learn — ML fraud scoring
- **Docker** + Docker Compose — one-command local setup
- **Lombok** — boilerplate reduction

## Design Patterns

| Pattern | Where Used |
|---------|-----------|
| Event-Driven Architecture | All inter-service communication via Kafka |
| CQRS | Analytics service separates read/write models |
| Outbox Pattern | Notification service persists before sending |
| Circuit Breaker | Rule-based fallback when ML model is unavailable |
| Repository Pattern | Spring Data JPA throughout |
| Saga Pattern | Transaction lifecycle managed via events |

## Quick Start (Docker — recommended)

**Prerequisites:** Docker and Docker Compose installed.

```bash
# 1. Clone the repo
git clone https://github.com/your-username/finsight.git
cd finsight

# 2. Copy environment config
cp .env.example .env

# 3. Start everything
docker compose up --build

# 4. Verify all services are healthy
curl http://localhost:8080/api/transactions/health
curl http://localhost:8082/api/analytics/health
curl http://localhost:8090/health
```

That's it. Docker Compose starts PostgreSQL, Kafka, Zookeeper, all Java services, and the ML model.

## Quick Start (Manual)

<details>
<summary>Expand for manual setup instructions</summary>

**Prerequisites:** Java 17+, Maven 3.9+, PostgreSQL 16, Kafka, Python 3.8+

### 1. Database
```bash
psql -U postgres -f setup-database.sql
```

### 2. Kafka (Linux/Mac)
```bash
# Terminal 1
bin/zookeeper-server-start.sh config/zookeeper.properties

# Terminal 2
bin/kafka-server-start.sh config/server.properties
```

### 3. ML Model
```bash
cd ml-model
pip install -r requirements.txt
python app.py
```

### 4. Java Services (separate terminals)
```bash
cd transaction-service && mvn spring-boot:run
cd fraud-detection-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

</details>

## API Examples

### Submit a Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 9500.00,
    "merchant": "Unknown Vendor",
    "currency": "GBP",
    "category": "Other"
  }'
```

### Get Transaction by ID
```bash
curl http://localhost:8080/api/transactions/{id}
```

### Get Analytics for Account
```bash
curl http://localhost:8082/api/analytics/550e8400-e29b-41d4-a716-446655440000
```

### Score via ML Model directly
```bash
curl -X POST http://localhost:8090/predict \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "abc-123",
    "amount": 9500.00,
    "merchant": "Unknown Vendor",
    "category": "Other"
  }'
```

## Kafka Topics

| Topic | Producer | Consumers |
|-------|----------|-----------|
| `transactions.raw` | transaction-service | fraud-detection, analytics |
| `transactions.flagged` | fraud-detection | notification-service |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `finsight` | Database name |
| `DB_USERNAME` | `postgres` | DB username |
| `DB_PASSWORD` | `postgres` | DB password |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker |
| `ML_MODEL_URL` | `http://localhost:8090` | ML service URL |

## Project Status

Beta/portfolio version demonstrating production-grade distributed systems architecture. The ML model trains on synthetic data at startup — in a production system this would load from a model registry.

**Planned improvements:**
- [ ] API Gateway service (scaffolded in `api-gateway/`)
- [ ] Kubernetes manifests
- [ ] Distributed tracing (OpenTelemetry)
- [ ] Metrics (Prometheus + Grafana)
- [ ] Integration test suite

## Developer

**Sai Kiran Reddy Gade** — Edinburgh, UK  
[GitHub](https://github.com/sai359) · [Portfolio](https://sai359.github.io/SKRG)
