-- V3__add_published_at.sql
-- Add published_at column to articles table

ALTER TABLE articles ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;
