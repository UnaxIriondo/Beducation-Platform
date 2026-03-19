# ⚙️ BeDucation Backend API

The backend for BeDucation Platform is a Spring Boot application that manages data persistence, security, and business logic for international student placements.

## 🚀 Key Features

- **Security**: OAuth2 Resource Server integrated with Auth0.
- **Database**: SQL Server / MySQL persistence with Spring Data JPA.
- **File Management**: AWS S3 integration for storing profiles and documents.
- **Bulk Import**: OpenCSV for rapid student data ingestion.
- **RESTful API**: Clean controller architecture with Swagger/OpenAPI documentation.

## 🛠️ Requirements

- **Java**: 17+
- **Maven**: 3+
- **Database**: SQL Server instance (local or remote)

## ⚙️ Configuration

The application is configured via `src/main/resources/application.yml`. Key configuration areas:

- **Database**: Datasource URL, username, and password.
- **Auth0**: Issuer URI and JWK-Set URI for token validation.
- **AWS**: S3 bucket name and access credentials (AWS IAM).
- **Mail**: SES or SMTP configuration for transaction emails.

> [!TIP]
> You can override any property using environment variables (e.g., `AUTH0_AUDIENCE`, `AWS_ACCESS_KEY`).

## 📖 API Documentation

The project uses **SpringDoc OpenAPI** to automatically generate documentation.

When the server is running, visit:
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **JSON Format**: `http://localhost:8080/api/v3/api-docs`

## 🧪 Development Workflow

1. **Build**: `mvn clean install`
2. **Run**: `mvn spring-boot:run`
3. **Tests**: `mvn test`

## 🗄️ Database Migrations

The project uses **Flyway** for database migrations. All migration scripts are located in:
`src/main/resources/db/migration`
