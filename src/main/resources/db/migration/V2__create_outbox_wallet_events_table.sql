CREATE TABLE outbox_wallet_events (
  id UUID PRIMARY KEY,                 -- UUIDv7 (Time-Sorted)
  wallet_id UUID NOT NULL,             -- Domain ID (Changed from aggregate_id)
  version BIGINT NOT NULL,             -- Versioning for Gap Detection / Ordering

  event_type VARCHAR(100) NOT NULL,    -- "WalletCreated", "MoneyDeposited", "WalletCreated"
  payload JSONB NOT NULL,              -- Full Event Data + Metadata (TraceId, ClientId)
  occurred_on TIMESTAMP WITH TIME ZONE NOT NULL,                         -- Business Time
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- System Time
);

-- Index For Query by Wallet (Time-Sorted)
CREATE INDEX idx_outbox_wallet_wallet_id
ON outbox_wallet_events(wallet_id);

-- Index For Concurrency Control (Optimistic Locking check)
CREATE UNIQUE INDEX idx_unique_outbox_wallet_wallet_version
ON outbox_wallet_events(wallet_id, version);

-- Index for Polling/CDC (Query/Scan for newly inserted events)
CREATE INDEX idx_outbox_wallet_created_at
ON outbox_wallet_events(created_at);

-- Documentation
COMMENT ON COLUMN outbox_wallet_events.version
IS 'Monotonic increasing version of the aggregate. Used for strict ordering and gap detection during reconciliation.';

COMMENT ON COLUMN outbox_wallet_events.occurred_on
IS 'Business Time: when the domain event actually happened (from Domain Model).';

COMMENT ON COLUMN outbox_wallet_events.created_at
IS 'System Time: when this record was inserted into the database. Used for Change Data Capture (CDC)/Polling.';

COMMENT ON COLUMN outbox_wallet_events.payload
IS 'JSONB content containing event data and metadata (for e.g., trace_id, client_id, etc).';

