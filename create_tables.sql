-- Create user table
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `nom` VARCHAR(255) NOT NULL,
  `prenom` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `numtlf` VARCHAR(50),
  `age` INT,
  `roles` VARCHAR(255) DEFAULT 'ROLE_USER'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create university table
CREATE TABLE IF NOT EXISTS `university` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `image` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create programme table
CREATE TABLE IF NOT EXISTS `programme` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `description` TEXT,
  `universityId` INT,
  FOREIGN KEY (`universityId`) REFERENCES `university`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create preferences table (if needed)
CREATE TABLE IF NOT EXISTS `preferences` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255),
  `id_user` INT,
  FOREIGN KEY (`id_user`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 