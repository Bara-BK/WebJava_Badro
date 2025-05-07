-- Create the password_reset_token table if it doesn't exist
CREATE TABLE IF NOT EXISTS password_reset_token (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiration TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Add 'used' column if it doesn't exist
ALTER TABLE password_reset_token ADD COLUMN IF NOT EXISTS used BOOLEAN DEFAULT FALSE; 