-- ============================================================
-- BeDucation Platform — Esquema de Base de Datos Inicial
-- Motor: MySQL 8+
-- ============================================================

-- 1. Tabla Usuarios base (Auth0)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auth0_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL, -- Enum: SCHOOL, STUDENT, COMPANY, ADMIN
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_auth0 (auth0_id)
);

-- 2. Tabla Escuelas
CREATE TABLE schools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    address VARCHAR(255),
    website VARCHAR(255),
    contact_person VARCHAR(255),
    phone VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    rejection_reason TEXT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_school_user (user_id),
    INDEX idx_school_status (status)
);

-- 3. Tabla Tipos de Formación (Diccionario)
CREATE TABLE education_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO education_types (name, code, description) VALUES
    ('FP Básica', 'FP_BASIC', 'Formación Profesional Básica'),
    ('FP Grado Medio', 'FP_MID', 'Ciclo Formativo de Grado Medio'),
    ('FP Grado Superior', 'FP_HIGH', 'Ciclo Formativo de Grado Superior'),
    ('Grado Universitario', 'UNI_DEGREE', 'Estudios Universitarios de Grado'),
    ('Máster Universitario', 'UNI_MASTER', 'Estudios Universitarios de Postgrado');

-- 4. Tabla Estudiantes
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    school_id BIGINT NOT NULL,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    invitation_email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    motivation TEXT,
    education_type_id BIGINT,
    profile_complete BOOLEAN NOT NULL DEFAULT 0,
    onboarding_complete BOOLEAN NOT NULL DEFAULT 0,
    invited_at TIMESTAMP,
    registered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (school_id) REFERENCES schools(id),
    FOREIGN KEY (education_type_id) REFERENCES education_types(id),
    INDEX idx_student_user (user_id),
    INDEX idx_student_school (school_id),
    INDEX idx_student_edu_type (education_type_id)
);

-- 5. Preferencias de Países del Estudiante
CREATE TABLE student_country_preferences (
    student_id BIGINT NOT NULL,
    country_code VARCHAR(100) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 6. Documentos del Estudiante AWS (CV, Cartas, Fotos)
CREATE TABLE student_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL, -- CV, COVER_LETTER, PHOTO
    s3_key VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    file_size_bytes BIGINT,
    content_type VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    INDEX idx_doc_student (student_id),
    INDEX idx_doc_type (document_type)
);

-- 7. Diccionarios de Competencias (Categorias)
CREATE TABLE keyword_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE
);

INSERT INTO keyword_categories (name) VALUES 
    ('Tecnología y Software'), 
    ('Marketing y Comunicación'), 
    ('Diseño y Creatividad'), 
    ('Empresa y Finanzas'), 
    ('Idiomas');

-- 8. Diccionario Competencias (Keywords)
CREATE TABLE keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES keyword_categories(id),
    INDEX idx_keyword_category (category_id)
);

INSERT INTO keywords (name, category_id) VALUES 
    ('Java', 1), ('Spring Boot', 1), ('Vue.js', 1), ('Python', 1),
    ('SEO', 2), ('Content Writing', 2), ('Social Media', 2),
    ('Figma', 3), ('UI/UX', 3), ('Ilustración', 3),
    ('Contabilidad', 4), ('Ventas B2B', 4), ('Analista de datos', 4),
    ('Inglés C1', 5), ('Alemán B2', 5), ('Francés Nativo', 5);

-- 9. Asociación N a N Estudiantes > Keywords
CREATE TABLE student_keywords (
    student_id BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, keyword_id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (keyword_id) REFERENCES keywords(id)
);

-- 10. Tabla Empresas (Company)
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    website VARCHAR(255),
    description TEXT,
    company_size VARCHAR(100),
    sector VARCHAR(100),
    logo_s3_key VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_company_user (user_id),
    INDEX idx_company_status (status)
);

-- 11. Ofertas Postuladas (Opportunity)
CREATE TABLE opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    start_date DATE,
    end_date DATE,
    spots INT NOT NULL DEFAULT 1,
    spots_available INT NOT NULL DEFAULT 1,
    education_type_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PENDING_REVIEW, APPROVED, CLOSED
    rejection_reason TEXT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (education_type_id) REFERENCES education_types(id),
    INDEX idx_opp_company (company_id),
    INDEX idx_opp_status (status),
    INDEX idx_opp_edu_type (education_type_id),
    INDEX idx_opp_country (country)
);

-- 12. Asociación N a N Oferta > Keywords requeridas
CREATE TABLE opportunity_keywords (
    opportunity_id BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    PRIMARY KEY (opportunity_id, keyword_id),
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    FOREIGN KEY (keyword_id) REFERENCES keywords(id)
);

-- 13. Solicitudes emitidas por estudiantes en 1 Oferta (Applications)
CREATE TABLE applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    stage INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED', -- APPLIED, INTERESTED, OFFERED, ADMIN_APPROVED, CONFIRMED, EXPIRED...
    rejection_reason TEXT,
    notes TEXT,
    expires_at TIMESTAMP NULL,
    applied_at TIMESTAMP NULL,
    status_updated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    INDEX idx_app_student (student_id),
    INDEX idx_app_opportunity (opportunity_id),
    INDEX idx_app_status (status),
    INDEX idx_app_stage (stage)
);

-- 14. Entrevistas (Interview) (1 a 1 de una Application)
CREATE TABLE interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE,
    scheduled_at TIMESTAMP NOT NULL,
    video_call_link VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(id)
);

-- Insertar Usuario Admin maestro por defecto para probar.
-- Modificar este insert posteriormente por el AUTH0_ID real del admin del tenant
INSERT INTO users (auth0_id, email, role, status) VALUES 
    ('auth0|admin-beducation', 'admin@beducation.com', 'ADMIN', 'ACTIVE');
