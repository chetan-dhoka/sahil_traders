-- ============================================================
-- V2  –  Multi-firm support  (Sahil Traders + S S Dhoka)
--
--  * RESETS all existing bills (they were test bills) so that
--    every firm starts its own numbering from 1.
--  * Sellers and buyers are NOT touched (shared by all firms).
--  * Every bill now belongs to exactly one firm.
-- ============================================================
SET NAMES utf8mb4;

-- ── 1. Clear test bills (bill_items go with them) ────────────
DELETE FROM bill_items;
DELETE FROM bills;

-- ── 2. Extra columns on the firm table ───────────────────────
ALTER TABLE firm_settings
    ADD COLUMN theme     VARCHAR(20)  NOT NULL DEFAULT 'MAROON',
    ADD COLUMN logo_text VARCHAR(10)  NULL,
    ADD COLUMN logo_path VARCHAR(255) NULL;

-- Existing firm = Sahil Traders (keeps its maroon look and "ST" circle)
UPDATE firm_settings
   SET theme = 'MAROON', logo_text = 'ST', logo_path = NULL
 ORDER BY id
 LIMIT 1;

-- ── 3. Add the second firm: S S Dhoka (navy theme + logo) ────
--      Tagline / footer lines are copied from Sahil Traders.
INSERT INTO firm_settings
    (firm_name, subtitle, address, mobile, pan_number,
     footer_line1, footer_line2, theme, logo_text, logo_path)
SELECT 'S S Dhoka', subtitle,
       'Girnar108, A-1401, Gangadham-Shatrunjay Road, Kondhwa Bk, Pune',
       '9822845854', 'AFHPD8556G',
       footer_line1, footer_line2,
       'NAVY', 'SSD', 'logos/ssd-logo.png'
  FROM firm_settings
 ORDER BY id
 LIMIT 1;

-- ── 4. Bills belong to a firm; bill numbers unique PER FIRM ──
ALTER TABLE bills ADD COLUMN firm_id BIGINT NOT NULL;
ALTER TABLE bills DROP INDEX bill_no;
ALTER TABLE bills
    ADD CONSTRAINT fk_bill_firm FOREIGN KEY (firm_id) REFERENCES firm_settings(id),
    ADD UNIQUE KEY uk_bill_firm_no (firm_id, bill_no);
CREATE INDEX idx_bill_firm_date ON bills(firm_id, bill_date);

-- ── 5. One bill-number counter per firm, both start at 1 ─────
DELETE FROM bill_sequence;
ALTER TABLE bill_sequence ALTER COLUMN last_no SET DEFAULT 0;
ALTER TABLE bill_sequence ADD COLUMN firm_id BIGINT NOT NULL;
ALTER TABLE bill_sequence
    ADD CONSTRAINT fk_seq_firm FOREIGN KEY (firm_id) REFERENCES firm_settings(id),
    ADD UNIQUE KEY uk_seq_firm (firm_id);
INSERT INTO bill_sequence (firm_id, last_no)
SELECT id, 0 FROM firm_settings;
