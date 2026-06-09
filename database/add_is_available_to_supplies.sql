USE officesync;

ALTER TABLE supplies
    ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE supplies
SET is_available = quantity_in_stock > 0;
