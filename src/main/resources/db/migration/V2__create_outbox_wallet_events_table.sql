CREATE TABLE outbox_wallet_events (
  id UUID PRIMARY KEY,                 -- UUIDv7 (Time-Sorted)
  aggregate_type VARCHAR(50) NOT NULL, -- "WALLET" (For Change Data Capture (CDC) Tools)
  aggregate_id UUID NOT NULL,          -- Wallet ID
  aggregate_version INTEGER NOT NULL,

  trace_id VARCHAR(64) NOT NULL UNIQUE,
  span_id VARCHAR(64) NOT NULL UNIQUE,

  event_type VARCHAR(100) NOT NULL,    -- "WalletCreated", "MoneyDeposited", "WalletCreated"
  payload JSONB NOT NULL,              -- Event Data
  occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,                         -- Domain Time
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- System Time
);

-- Index For Query Wallet (Time-Sorted)
CREATE INDEX idx_outbox_wallet_aggregate_id
ON outbox_wallet_events(aggregate_id);

CREATE INDEX idx_outbox_wallet_created_at
ON outbox_wallet_events(created_at);
