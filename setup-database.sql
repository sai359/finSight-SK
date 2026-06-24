-- FinSight Database Setup
-- Run this in PostgreSQL before starting services

CREATE DATABASE finsight;

\c finsight;

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL,
    amount      DECIMAL(15,2) NOT NULL,
    currency    VARCHAR(3) DEFAULT 'GBP',
    merchant    VARCHAR(255) NOT NULL,
    category    VARCHAR(100),
    location    VARCHAR(255),
    status      VARCHAR(20) DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Fraud alerts table
CREATE TABLE IF NOT EXISTS fraud_alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id  VARCHAR(255) NOT NULL,
    account_id      VARCHAR(255) NOT NULL,
    risk_score      DECIMAL(5,4),
    risk_level      VARCHAR(20),
    reasons         TEXT,
    model_version   VARCHAR(50),
    reviewed        BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_fraud_alerts_transaction ON fraud_alerts(transaction_id);
CREATE INDEX idx_fraud_alerts_account ON fraud_alerts(account_id);

-- Spending analytics table
CREATE TABLE IF NOT EXISTS spending_analytics (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  VARCHAR(255) NOT NULL,
    period      DATE,
    category    VARCHAR(100),
    total_spent DECIMAL(15,2) DEFAULT 0,
    tx_count    INTEGER DEFAULT 0,
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_analytics_account ON spending_analytics(account_id);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      VARCHAR(255),
    transaction_id  VARCHAR(255),
    type            VARCHAR(50),
    message         TEXT,
    risk_level      VARCHAR(20),
    risk_score      DECIMAL(5,4),
    sent_at         TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_notifications_account ON notifications(account_id);

-- Sample test data
INSERT INTO transactions (account_id, amount, merchant, category, status)
VALUES
  ('550e8400-e29b-41d4-a716-446655440000', 49.99, 'Amazon', 'Shopping', 'COMPLETED'),
  ('550e8400-e29b-41d4-a716-446655440000', 120.00, 'Sainsburys', 'Groceries', 'COMPLETED'),
  ('550e8400-e29b-41d4-a716-446655440000', 9500.00, 'Unknown Vendor', 'Other', 'FLAGGED');

SELECT 'FinSight database setup complete!' as status;
