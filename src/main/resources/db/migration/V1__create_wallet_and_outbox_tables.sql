CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE wallets(
  id                     UUID PRIMARY KEY,
  name                   VARCHAR(255)             NOT NULL,
  balance_amount         NUMERIC(19, 4)           NOT NULL DEFAULT 0,
  balance_currency       VARCHAR(3)               NOT NULL,
  linked_bank_account_id UUID,
  owner_id               UUID                     NOT NULL,
  status                 VARCHAR(20)              NOT NULL,
  version                BIGINT                   NOT NULL DEFAULT 0,
  created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_wallets_owner_id ON wallets (owner_id);

-- Outbox Events table
CREATE TABLE outbox_wallet_events(
  id              UUID PRIMARY KEY,                                            -- UUIDv7 (Time-Sorted)
  wallet_id       UUID                     NOT NULL, -- Domain ID (Changed from aggregate_id)
  version BIGINT NOT NULL,                                                     -- Versioning for Gap Detection / Ordering

  event_type      VARCHAR(100)             NOT NULL,                           -- "WalletCreated", "MoneyDeposited", "WalletCreated"
  payload         JSONB                    NOT NULL,                           -- Full Event Data + Metadata (TraceId, ClientId)
  occurred_on     TIMESTAMP WITH TIME ZONE NOT NULL,                           -- Business Time
  created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- System Time

  delivery_status VARCHAR(20)              NOT NULL DEFAULT 'PENDING',
  published_at    TIMESTAMP WITH TIME ZONE,                                    -- Nullable: NULL=Pending/Failed, Value=Published

  CONSTRAINT chk_delivery_status CHECK (delivery_status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

-- Partial Index for High Performance Polling (The "Hot Path")
-- Only indexes 'PENDING' events, keeping the index size tiny and lookups lightning fast.
-- This allows the Poller to ignore millions of 'PUBLISHED' events without scanning them.
-- NOTE: 'FAILED' events are ignored here to prevent poison pills from blocking the queue.
CREATE INDEX idx_outbox_wallet_pending
  ON outbox_wallet_events (created_at)
  WHERE delivery_status = 'PENDING';

-- Index For Query by Wallet (Time-Sorted)
CREATE INDEX idx_outbox_wallet_wallet_id
  ON outbox_wallet_events (wallet_id);

-- Index For Concurrency Control (Optimistic Locking check)
CREATE UNIQUE INDEX idx_unique_outbox_wallet_wallet_version
  ON outbox_wallet_events (wallet_id, version);

-- Documentation for future maintainers (Why no FK?)
COMMENT ON TABLE outbox_wallet_events
  IS 'Stores domain events for the Transactional Outbox pattern. Designed for high-throughput writes.';

COMMENT ON COLUMN outbox_wallet_events.wallet_id
  IS 'Refers to wallets.id. Intentionally NO FK constraint to allow for future sharding/partitioning strategies and avoid locking contention during high-concurrency writes.';

-- Documentation
COMMENT ON COLUMN outbox_wallet_events.version
  IS 'Monotonic increasing version of the aggregate. Used for strict ordering and gap detection during reconciliation.';

COMMENT ON COLUMN outbox_wallet_events.occurred_on
  IS 'Business Time: when the domain event actually happened (from Domain Model).';

COMMENT ON COLUMN outbox_wallet_events.payload
  IS 'JSONB content containing event data and metadata (for e.g., trace_id, client_id, etc).';
