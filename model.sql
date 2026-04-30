
-- ============================
-- * ms1: customer-service
-- ============================

-- ============================
-- TABLE: persons
-- ============================

CREATE TABLE persons (
    person_id        BIGSERIAL     PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    gender           VARCHAR(10)   NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    identification   VARCHAR(10)   NOT NULL UNIQUE,
    address          VARCHAR(200)  NOT NULL,
    phone            VARCHAR(10)   NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    version          BIGINT        NOT NULL DEFAULT 0
);

-- ============================
-- TABLE: customers
-- ============================

CREATE TABLE customers (
    customer_id      BIGINT        PRIMARY KEY REFERENCES persons(person_id),
    password         VARCHAR(100)  NOT NULL,
    status           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    version          BIGINT        NOT NULL DEFAULT 0
);

-- ============================
-- * ms2: account-service
-- ============================

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
    created_at             TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP     NOT NULL DEFAULT NOW(),    
    version                BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_movements_account_id ON movements(account_id);
CREATE INDEX idx_movements_date       ON movements(date);