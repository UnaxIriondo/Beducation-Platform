#  BeDucation Platform
# AÑADIR QUE BORJA PUEDA IMPORTARLOS COMO ADMIN LOS CSV Y QUE LUEGO LOS ALUMNOS RELLENEN SUS DATOS.

BeDucation is a comprehensive platform designed to streamline the management of international student placements. It serves as a bridge between schools, students, and companies, providing a unified dashboard for tracking applications, managing profiles, and coordinating the internship process.

##  Technology Stack

### Backend
- **Core**: Java 17, Spring Boot 3.2
- **Security**: Spring Security with OAuth2 Resource Server (Auth0 integration)
- **Database**: SQL Server / MySQL
- **ORM**: Spring Data JPA (Hibernate)
- **Migrations**: Flyway
- **Cloud Storage**: AWS S3 (for CVs, photos, and logos)
- **Email**: Spring Mail / AWS SES
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Utilities**: Lombok, OpenCSV (for massive student imports)

### Frontend
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **State Management**: Pinia
- **Routing**: Vue Router
- **Styling**: Tailwind CSS
- **Icons**: Lucide Icons
- **Authentication**: Auth0 Vue SDK
- **HTTP Client**: Axios

---

##  Project Structure

```text
Beducation-Platform/
├── beducation-backend/     # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── README.md           # Backend-specific instructions
├── beducation-frontend/    # Vue.js Application
│   ├── src/
│   ├── package.json
│   └── README.md           # Frontend-specific instructions
├── beducation.sql          # Base database schema
├── datosPrueba.sql         # Sample/Test data
└── README.md               # Main project documentation
```

---

##  Getting Started

### 1. Prerequisites
- **Java**: JDK 17
- **Node.js**: v18 or later
- **Database**: SQL Server (configured in `application.yml`)
- **Maven**: Latest version

### 2. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd beducation-backend
   ```
2. Configure your environment variables or update `src/main/resources/application.yml` with your database and Auth0 credentials.
3. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
4. Access the API documentation (Swagger) at:
   `http://localhost:8080/api/swagger-ui.html`

### 3. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd beducation-frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
4. Open your browser at:
   `http://localhost:5173`

---

##  Key Features

- **Multi-role Support**: Dashboards tailored for Administrators, School Coordinators, and Students.
- **Seamless Authentication**: Integrated with Auth0 for secure login and JWT-based API protection.
- **Data Import**: High-performance CSV parsing for importing hundreds of student records instantly.
- **Cloud Integrated**: Automatic document management using AWS S3.
- **Modern UI**: Clean, light/dark mode compatible interface built with Tailwind CSS.

---

##  License
*Proprietary - BeDucation Platform*
