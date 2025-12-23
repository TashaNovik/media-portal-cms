-- V5__fix_episodes_table.sql
-- Fix episodes table: remove NOT NULL from episode_number, drop unused updated_at column

-- Make episode_number nullable (it was NOT NULL but model doesn't require it)
ALTER TABLE episodes ALTER COLUMN episode_number DROP NOT NULL;

-- Drop updated_at column from episodes (not present in Entity model)
ALTER TABLE episodes DROP COLUMN IF EXISTS updated_at;
