# BADRO - University Exchange & Experience Management System

## Overview
BADRO is a comprehensive JavaFX application designed to facilitate international university exchanges, study abroad experiences, and educational event management. The application helps students explore exchange opportunities, share their experiences abroad, apply for programs, and participate in educational events.

## Features

### 🌍 Experience Sharing
- Create and share study abroad experiences with destination, description, and photos
- Comment and like other students' experiences
- Browse experiences by destination or university

### 🎓 University Exchange Programs
- Browse available exchange programs at partner universities
- Apply for exchange programs with personalized motivation letters
- Track application status (Pending, Approved, Rejected)
- Access university guides with detailed information about studying abroad

### 📅 Event Management
- View and register for educational events (workshops, seminars, fairs)
- Manage event participations
- Generate event statistics and reports
- Automated ticket generation for event participation

### 👤 User Management
- Secure account creation and authentication
- Password reset functionality
- User profile management
- Role-based access control (Student, Admin)

### 🔍 Preference-Based Recommendations
- Create and manage study abroad preferences
- Receive personalized program recommendations based on preferences
- Filter universities and programs by country, teaching language, and domain

## Technical Stack

- **Language:** Java 17
- **UI Framework:** JavaFX 21.0.6
- **Build Tool:** Maven
- **Database:** MySQL
- **Additional Libraries:**
  - iText (PDF generation)
  - ZXing (QR code generation)
  - MySQL Connector
  - Project Lombok

## Getting Started

### Prerequisites
- Java JDK 17 or higher
- MySQL Server
- Maven
- IntelliJ IDEA (recommended)

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/badro.git
cd badro
```

2. Set up the MySQL database
```bash
mysql -u root -p < jacemdb.sql
```

3. Configure the database connection
   - Open `src/main/java/tn/badro/tools/MyDataBase.java`
   - Modify connection details if needed

4. Build the project
```bash
mvn clean install
```

5. Run the application
```bash
mvn javafx:run
```

### Project Structure

- `src/main/java/tn/badro/entities`: Model classes
- `src/main/java/tn/badro/services`: Business logic and database operations
- `src/main/java/tn/badro/Controllers`: JavaFX controllers
- `src/main/java/tn/badro/tools`: Utility classes
- `src/main/resources`: FXML layouts, CSS styles, and images

## Contributors

- [Jacem Gasmi](mailto:jacem.gasmi@esprit.tn)
- [Bara Ben khedher](mailto:bara.benkhedher@esprit.tn)
- [Sinda Essadi](mailto:sinda.essaadi@esprit.tn)
- [Mahdi Daly](mailto:mahdi.daly@esprit.tn)
- [Hedi Sliti](mailto:mohamedhedi.sliti@esprit.tn)

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Keywords
University Exchange, Study Abroad, Event Management, JavaFX Application, Student Experiences, Program Applications, Educational Events, International Education 