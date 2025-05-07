-- Check if table exists and create it if not
CREATE TABLE IF NOT EXISTS evenement (
    event_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date DATE,
    heure TIME,
    lieu VARCHAR(255),
    type VARCHAR(100),
    organisateur_nom VARCHAR(255),
    nombre_max_participants INT,
    status VARCHAR(50),
    ticket_prix VARCHAR(50),
    periode_inscription_fin VARCHAR(50)
);

-- Add example event for testing
INSERT INTO evenement (
    titre, 
    description, 
    date, 
    heure, 
    lieu, 
    type, 
    organisateur_nom, 
    nombre_max_participants, 
    status, 
    ticket_prix, 
    periode_inscription_fin
) SELECT 
    'Spring Exchange Program', 
    'Learn about international exchange opportunities', 
    CURRENT_DATE(), 
    '15:00:00', 
    'Main Auditorium', 
    'Educational', 
    'International Office', 
    100, 
    'Open', 
    '25.00', 
    '2023-12-31'
FROM DUAL 
WHERE NOT EXISTS (SELECT 1 FROM evenement WHERE titre = 'Spring Exchange Program' LIMIT 1); 