-- ============================================================
-- V5: Add full_name to users table
-- ============================================================

ALTER TABLE users ADD full_name NVARCHAR(255) NULL;
