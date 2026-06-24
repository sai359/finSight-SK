"""
FinSight ML Fraud Detection Model
Isolation Forest — anomaly detection for fraud scoring
Port: 8090
"""

from flask import Flask, request, jsonify
from sklearn.ensemble import IsolationForest
import numpy as np
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# ── Train model on startup with synthetic data ─────────────────────
# In production: load from a trained model file (joblib/pickle)
def train_model():
    logger.info("Training Isolation Forest model...")
    np.random.seed(42)

    # Synthetic normal transaction data for initial training
    # Features: [amount, hour_of_day, day_of_week, is_round_number, category_risk]
    normal_data = np.random.randn(1000, 5)
    normal_data[:, 0] = np.abs(normal_data[:, 0]) * 100 + 50  # amount: 0-500
    normal_data[:, 1] = np.random.randint(8, 22, 1000)        # hour: 8am-10pm
    normal_data[:, 2] = np.random.randint(0, 7, 1000)         # day: 0-6
    normal_data[:, 3] = np.random.randint(0, 2, 1000)         # round: 0 or 1
    normal_data[:, 4] = np.random.uniform(0, 0.3, 1000)       # category risk: low

    model = IsolationForest(
        n_estimators=100,
        contamination=0.1,  # expect 10% anomalies
        random_state=42
    )
    model.fit(normal_data)
    logger.info("Model trained successfully")
    return model

model = train_model()

# ── Feature extraction ──────────────────────────────────────────────
def extract_features(transaction):
    amount = float(transaction.get("amount", 0))
    merchant = transaction.get("merchant", "").lower()
    category = transaction.get("category", "").lower()

    # Feature 1: Amount (normalised)
    amount_feature = min(amount / 10000, 1.0)

    # Feature 2: Hour of day (0-23, suspicious if night)
    import datetime
    hour = datetime.datetime.now().hour
    hour_feature = hour / 23.0

    # Feature 3: Day of week
    day_feature = datetime.datetime.now().weekday() / 6.0

    # Feature 4: Round number (0 or 1)
    round_feature = 1.0 if amount % 100 == 0 else 0.0

    # Feature 5: Category risk score
    high_risk_categories = ["casino", "gambling", "cryptocurrency", "adult"]
    medium_risk_categories = ["nightclub", "bar", "liquor"]
    category_risk = 0.0
    for cat in high_risk_categories:
        if cat in category or cat in merchant:
            category_risk = 1.0
            break
    if category_risk == 0.0:
        for cat in medium_risk_categories:
            if cat in category or cat in merchant:
                category_risk = 0.5
                break

    return np.array([[
        amount_feature,
        hour_feature,
        day_feature,
        round_feature,
        category_risk
    ]])

# ── Risk scoring ────────────────────────────────────────────────────
def calculate_risk_score(transaction):
    features = extract_features(transaction)

    # Isolation Forest score: negative = anomaly
    raw_score = model.score_samples(features)[0]

    # Convert to 0-1 risk score (higher = more risky)
    # Typical range: -0.5 to 0.5
    risk_score = max(0.0, min(1.0, (0.5 - raw_score)))

    # Apply business rules on top
    amount = float(transaction.get("amount", 0))
    if amount > 10000:
        risk_score = max(risk_score, 0.8)
    elif amount > 5000:
        risk_score = max(risk_score, 0.5)

    return round(risk_score, 4)

def get_risk_level(score):
    if score >= 0.7:   return "BLOCK"
    elif score >= 0.4: return "REVIEW"
    else:              return "NORMAL"

def get_reasons(transaction, score):
    reasons = []
    amount = float(transaction.get("amount", 0))
    category = (transaction.get("category") or "").lower()
    merchant  = (transaction.get("merchant") or "").lower()

    if amount > 5000:
        reasons.append(f"High value transaction: £{amount}")
    if any(r in category or r in merchant for r in ["casino","gambling","crypto"]):
        reasons.append("High-risk merchant category")
    if amount % 100 == 0:
        reasons.append("Suspicious round amount")
    if score >= 0.7:
        reasons.append("ML model detected anomaly pattern")

    return reasons if reasons else ["Transaction within normal parameters"]

# ── API Endpoints ───────────────────────────────────────────────────
@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "service": "FinSight ML Model", "model": "IsolationForest-v1.0"})

@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data provided"}), 400

        transaction_id = data.get("transactionId", "unknown")
        logger.info(f"Scoring transaction: {transaction_id}")

        risk_score = calculate_risk_score(data)
        risk_level = get_risk_level(risk_score)
        reasons    = get_reasons(data, risk_score)

        response = {
            "transactionId": transaction_id,
            "riskScore":     risk_score,
            "riskLevel":     risk_level,
            "reasons":       reasons,
            "modelVersion":  "isolation-forest-v1.0"
        }

        logger.info(f"Transaction {transaction_id}: score={risk_score}, level={risk_level}")
        return jsonify(response)

    except Exception as e:
        logger.error(f"Prediction error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route("/batch-predict", methods=["POST"])
def batch_predict():
    try:
        transactions = request.get_json()
        if not isinstance(transactions, list):
            return jsonify({"error": "Expected a list of transactions"}), 400

        results = []
        for tx in transactions:
            risk_score = calculate_risk_score(tx)
            results.append({
                "transactionId": tx.get("transactionId"),
                "riskScore":     risk_score,
                "riskLevel":     get_risk_level(risk_score)
            })

        return jsonify(results)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    logger.info("Starting FinSight ML Model on port 8090")
    app.run(host="0.0.0.0", port=8090, debug=False)
