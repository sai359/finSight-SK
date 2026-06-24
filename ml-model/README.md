# FinSight ML Fraud Detection Model

Isolation Forest anomaly detection model for real-time fraud scoring.

## Setup

```bash
pip install -r requirements.txt
python app.py
```

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /health | Health check |
| POST | /predict | Score single transaction |
| POST | /batch-predict | Score multiple transactions |

## Example

```bash
curl -X POST http://localhost:8090/predict \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-001",
    "accountId": "acc-123",
    "amount": 9500,
    "merchant": "Unknown Vendor",
    "category": "Other"
  }'
```

## Response

```json
{
  "transactionId": "tx-001",
  "riskScore": 0.82,
  "riskLevel": "BLOCK",
  "reasons": ["High value transaction: £9500.0", "ML model detected anomaly pattern"],
  "modelVersion": "isolation-forest-v1.0"
}
```

## Risk Levels

| Score | Level | Action |
|-------|-------|--------|
| 0.0 – 0.4 | NORMAL | Allow |
| 0.4 – 0.7 | REVIEW | Flag for review |
| 0.7 – 1.0 | BLOCK | Block transaction |
