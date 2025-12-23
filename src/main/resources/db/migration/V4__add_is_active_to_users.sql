-- V4__add_is_active_to_users.sql
-- Add is_active column to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;
