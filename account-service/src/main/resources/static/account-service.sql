-- ============================
-- * ms2: account-service
-- ============================

CREATE DATABASE account_service;
\c account_service;

-- ============================
-- TABLE: accounts
-- ============================

CREATE TABLE accounts (
    account_id        BIGSERIAL     PRIMARY KEY,
    account_number    VARCHAR(10)   NOT NULL UNIQUE,
    account_type      VARCHAR(10)   NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    initial_balance   NUMERIC(15,2) NOT NULL CHECK (initial_balance >= 0),
    available_balance NUMERIC(15,2) NOT NULL CHECK (available_balance >= 0),
    status            BOOLEAN       NOT NULL DEFAULT TRUE,
    customer_id       BIGINT        NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    version           BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);

-- ============================
-- TABLE: movements
-- ============================

CREATE TABLE movements (
    movement_id            BIGSERIAL     PRIMARY KEY,
    date                   TIMESTAMP     NOT NULL DEFAULT NOW(),
    movement_type          VARCHAR(10)   NOT NULL CHECK (movement_type IN ('DEBIT', 'CREDIT')),
    amount                 NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    balance_after_movement NUMERIC(15,2) NOT NULL,
    account_id             BIGINT        NOT NULL REFERENCES accounts(account_id),   
    version                BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_movements_account_id ON movements(account_id);
CREATE INDEX idx_movements_date       ON movements(date);

-- RELACIONES

ALTER TABLE accounts
ADD CONSTRAINT fk_accounts_customers
FOREIGN KEY (customer_id)
REFERENCES customers(customer_id)
ON DELETE CASCADE
ON UPDATE CASCADE;

ALTER TABLE movements
ADD CONSTRAINT fk_movements_accounts
FOREIGN KEY (account_id)
REFERENCES accounts(account_id)
ON DELETE CASCADE
ON UPDATE CASCADE;


-- Función genérica para actualizar el campo updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para la tabla accounts
CREATE TRIGGER trg_accounts_updated_at
BEFORE UPDATE ON accounts
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Trigger para la tabla movements
CREATE TRIGGER trg_movements_updated_at
BEFORE UPDATE ON movements
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

