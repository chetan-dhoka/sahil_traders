-- ============================================================
-- Sahil Traders Billing System
-- V1__init.sql  –  Full schema + seed data
-- ============================================================
SET NAMES utf8mb4;

-- ── FIRM SETTINGS (single row, editable from UI) ─────────────
CREATE TABLE firm_settings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    firm_name       VARCHAR(100) NOT NULL DEFAULT 'Sahil Traders',
    subtitle        VARCHAR(150)          DEFAULT 'Grain & Grocery Broker',
    address         VARCHAR(255)          DEFAULT 'Anand Heights, Kondhwa, Pune 48.',
    mobile          VARCHAR(30)           DEFAULT '9999999999',
    pan_number      VARCHAR(20)           DEFAULT 'COHPC2282R',
    footer_line1    VARCHAR(300)          DEFAULT 'चूकभूल देणे घेणे.',
    footer_line2    VARCHAR(300)          DEFAULT 'व्यापारी संबंधात आम्ही फक्त साक्षीदार राहू.',
    updated_at      DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO firm_settings (firm_name, subtitle, address, mobile, pan_number, footer_line1, footer_line2)
VALUES ('Sahil Traders', 'Grain & Grocery Broker',
        'Anand Heights, Kondhwa, Pune 48.',
        '9550081765',
        'COHPC2282R',
        'चूकभूल देणे घेणे.',
        'व्यापारी संबंधात आम्ही फक्त साक्षीदार राहू.');

-- ── SELLERS (Name field on bill) ─────────────────────────────
CREATE TABLE sellers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    name_lower      VARCHAR(150) NOT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seller_name (name_lower)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_seller_name ON sellers(name_lower);

-- ── BUYERS (Particulars column on bill) ──────────────────────
CREATE TABLE buyers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    name_lower      VARCHAR(150) NOT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_buyer_name (name_lower)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_buyer_name ON buyers(name_lower);

-- ── BILL SEQUENCE ─────────────────────────────────────────────
CREATE TABLE bill_sequence (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    last_no     BIGINT NOT NULL DEFAULT 183
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Start from 184 (183 is last hand-written bill)
INSERT INTO bill_sequence (last_no) VALUES (183);

-- ── BILLS ─────────────────────────────────────────────────────
CREATE TABLE bills (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_no      BIGINT       NOT NULL UNIQUE,
    bill_date    DATE         NOT NULL,
    seller_id    BIGINT       NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_seller FOREIGN KEY (seller_id) REFERENCES sellers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_bill_date   ON bills(bill_date);
CREATE INDEX idx_bill_seller ON bills(seller_id);

-- ── BILL ITEMS ────────────────────────────────────────────────
CREATE TABLE bill_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id       BIGINT        NOT NULL,
    item_date     DATE          NOT NULL,
    buyer_id      BIGINT        NOT NULL,
    material_type VARCHAR(100)  NOT NULL,
    qty           INT           NOT NULL,
    rate          DECIMAL(10,2) NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    sort_order    INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_item_bill  FOREIGN KEY (bill_id)  REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_buyer FOREIGN KEY (buyer_id) REFERENCES buyers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_item_bill  ON bill_items(bill_id);
CREATE INDEX idx_item_buyer ON bill_items(buyer_id);
CREATE INDEX idx_item_date  ON bill_items(item_date);

-- ── SEED: Sample Sellers ──────────────────────────────────────
INSERT INTO sellers (name, name_lower) VALUES
('Ravi Kumar',     'ravi kumar'),
('Suresh Patil',   'suresh patil'),
('Mahesh Shah',    'mahesh shah'),
('Dinesh Agrawal', 'dinesh agrawal'),
('Prakash Joshi',  'prakash joshi');

-- ── SEED: Sample Buyers ───────────────────────────────────────
INSERT INTO buyers (name, name_lower) VALUES
('Ramesh Yadav',   'ramesh yadav'),
('Sanjay Mehta',   'sanjay mehta'),
('Vijay Sharma',   'vijay sharma'),
('Anil Gupta',     'anil gupta'),
('Rajendra Patel', 'rajendra patel'),
('Mohan Verma',    'mohan verma'),
('Sunil Desai',    'sunil desai');

-- ── SEED: Sample Bill (Bill #184) ────────────────────────────
INSERT INTO bills (bill_no, bill_date, seller_id, total_amount)
VALUES (184, '2024-04-30', 1, 0);

INSERT INTO bill_items (bill_id, item_date, buyer_id, material_type, qty, rate, amount, sort_order) VALUES
(1, '2024-04-10', 1, 'Wheat',  200, 2.50,  500.00, 1),
(1, '2024-04-15', 2, 'Rice',   150, 3.00,  450.00, 2),
(1, '2024-04-20', 3, 'Poha',   100, 1.75,  175.00, 3),
(1, '2024-04-25', 4, 'Wheat',  300, 2.50,  750.00, 4);

UPDATE bills SET total_amount = 1875.00 WHERE id = 1;
UPDATE bill_sequence SET last_no = 184;
