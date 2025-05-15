-- Add destination column to the experience table
ALTER TABLE experience ADD COLUMN destination VARCHAR(100);

-- Update all existing records to have a default empty destination
UPDATE experience SET destination = '' WHERE destination IS NULL; 