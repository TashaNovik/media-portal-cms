-- V2__add_view_count.sql
-- Add view_count column to content tables

ALTER TABLE articles ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;
ALTER TABLE videos ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;
ALTER TABLE podcasts ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;
