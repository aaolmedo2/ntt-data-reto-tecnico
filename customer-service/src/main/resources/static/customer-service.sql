
-- ============================
-- * ms1: customer-service
-- ============================

CREATE DATABASE customer_service;
\c customer_service;

-- ============================
-- TABLE: persons
-- ============================

CREATE TABLE persons (
    person_id        BIGSERIAL     PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    gender           VARCHAR(10)   NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    identification VARCHAR(10) NOT NULL UNIQUE CHECK (char_length(identification) = 10 AND identification ~ '^[0-9]+$'),
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
    password         VARCHAR(255)  NOT NULL,
    status           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    version          BIGINT        NOT NULL DEFAULT 0
);

-- RELACIONES

ALTER TABLE customers
ADD CONSTRAINT fk_customers_persons
FOREIGN KEY (customer_id)
REFERENCES persons(person_id)
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

-- Trigger para la tabla persons
CREATE TRIGGER trg_persons_updated_at
BEFORE UPDATE ON persons
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Trigger para la tabla customers
CREATE TRIGGER trg_customers_updated_at
BEFORE UPDATE ON customers
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
