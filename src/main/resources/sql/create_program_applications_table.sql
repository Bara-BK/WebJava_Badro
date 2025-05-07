-- Create program_applications table
CREATE TABLE IF NOT EXISTS program_applications (
    application_id VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    programme_id INT NOT NULL,
    motivation_letter TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    application_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (programme_id) REFERENCES programme(id) ON DELETE CASCADE
);

-- Add index for faster queries
CREATE INDEX IF NOT EXISTS idx_program_applications_user_id ON program_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_program_applications_programme_id ON program_applications(programme_id);
CREATE INDEX IF NOT EXISTS idx_program_applications_status ON program_applications(status); 