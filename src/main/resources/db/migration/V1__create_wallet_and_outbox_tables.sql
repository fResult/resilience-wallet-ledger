CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE wallets (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  balance_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
  balance_currency VARCHAR(3) NOT NULL,
  linked_bank_account_id UUID,
  owner_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT chk_wallet_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_wallets_owner_id ON wallets(owner_id);

-- Outbox Events table
CREATE TABLE outbox_wallet_events (
  id UUID PRIMARY KEY,
  aggregate_id UUID, -- Wallet ID
  event_type VARCHAR(100) NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  delivery_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

  CONSTRAINT chk_delivery_status CHECK (delivery_status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

-- Partial Index for High Performance Polling
CREATE INDEX idx_outbox_pending
ON outbox_wallet_events(created_at)
WHERE delivery_status = 'PENDING';

-- Documentation for future maintainers (Why no FK?)
COMMENT ON TABLE outbox_wallet_events IS 'Stores domain events for the Transactional Outbox pattern. Designed for high-throughput writes.';
COMMENT ON COLUMN outbox_wallet_events.aggregate_id IS 'Refers to wallets.id. Intentionally NO FK constraint to allow for future sharding/partitioning strategies and avoid locking contention during high-concurrency writes.';
