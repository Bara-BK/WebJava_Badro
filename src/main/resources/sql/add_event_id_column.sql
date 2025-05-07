-- Check if event_id column exists
SET @columnExists = 0;
SELECT COUNT(*) INTO @columnExists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'evenement' 
AND COLUMN_NAME = 'event_id';

-- Add event_id column if it doesn't exist
SET @query = IF(@columnExists > 0, 
    'SELECT "Column event_id already exists"', 
    'ALTER TABLE evenement ADD COLUMN event_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST');

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt; 