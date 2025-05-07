-- Events and Participation Tables Setup Script

-- Create the events table
CREATE TABLE IF NOT EXISTS evenement (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    heure TIME,
    lieu VARCHAR(255),
    type VARCHAR(50),
    organisateur_nom VARCHAR(255),
    nombre_max_participants INT,
    status VARCHAR(50) DEFAULT 'Active',
    ticket_prix VARCHAR(50),
    periode_inscription_fin VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create the participation table
CREATE TABLE IF NOT EXISTS participation (
    participant_id INT AUTO_INCREMENT PRIMARY KEY,
    id_event INT NOT NULL,
    nom_participant VARCHAR(255) NOT NULL,
    date_inscription DATE DEFAULT (CURRENT_DATE),
    evenement_nom VARCHAR(255),
    telephone_number INT,
    ticket_code VARCHAR(20) UNIQUE,
    paiment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_event) REFERENCES evenement(event_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create index for faster queries
CREATE INDEX idx_participation_event ON participation(id_event);
CREATE INDEX idx_event_date ON evenement(date);
CREATE INDEX idx_event_type ON evenement(type);

-- Sample event data (optional)
-- INSERT INTO evenement (titre, description, date, heure, lieu, type, organisateur_nom, nombre_max_participants, status, ticket_prix, periode_inscription_fin) VALUES
-- ('Welcome Conference', 'Join us for the welcome conference for exchange students', '2024-09-15', '14:00:00', 'Main Auditorium', 'Educational', 'International Relations Office', 200, 'Active', 'Free', '2024-09-10'),
-- ('Cultural Exchange Fair', 'Discover different cultures from our partner universities', '2024-10-05', '10:00:00', 'University Campus', 'Cultural', 'Student Association', 500, 'Active', '$5', '2024-10-01'),
-- ('Sports Tournament', 'International students sports competition', '2024-11-20', '09:00:00', 'University Sports Center', 'Sports', 'Sports Department', 100, 'Active', '$10', '2024-11-15');

-- Sample participation data (optional)
-- INSERT INTO participation (id_event, nom_participant, evenement_nom, telephone_number, ticket_code, paiment_method) VALUES
-- (1, 'John Doe', 'Welcome Conference', 12345678, 'WC001', 'Free'),
-- (1, 'Jane Smith', 'Welcome Conference', 87654321, 'WC002', 'Free'),
-- (2, 'Alex Johnson', 'Cultural Exchange Fair', 23456789, 'CEF001', 'Cash'),
-- (3, 'Maria Garcia', 'Sports Tournament', 34567890, 'ST001', 'Online'); 