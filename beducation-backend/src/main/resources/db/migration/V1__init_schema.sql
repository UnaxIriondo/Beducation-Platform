-- ============================================================
-- BeDucation Platform — Esquema de Base de Datos Inicial
-- Motor: SQL Server (T-SQL)
-- ============================================================

-- 1. Tabla Usuarios base (Auth0)
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    auth0_id NVARCHAR(255) NOT NULL UNIQUE,
    email NVARCHAR(255) NOT NULL UNIQUE,
    role NVARCHAR(50) NOT NULL, -- Enum: SCHOOL, STUDENT, COMPANY, ADMIN
    status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    -- Indexing for performance
);

CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_auth0 ON users (auth0_id);

-- 2. Tabla Escuelas
CREATE TABLE schools (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name NVARCHAR(255) NOT NULL,
    country NVARCHAR(100) NOT NULL,
    city NVARCHAR(100),
    address NVARCHAR(255),
    website NVARCHAR(255),
    contact_person NVARCHAR(255),
    phone NVARCHAR(50),
    institution_type NVARCHAR(50),
    description NVARCHAR(MAX),
    status NVARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    rejection_reason NVARCHAR(MAX),
    approved_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_school_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_school_user ON schools (user_id);
CREATE INDEX idx_school_status ON schools (status);

-- 3. Tabla Tipos de Formación (Diccionario)
CREATE TABLE education_types (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL UNIQUE,
    code NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255)
);

INSERT INTO education_types (name, code, description) VALUES
    ('FP Básica', 'FP_BASIC', 'Formación Profesional Básica'),
    ('FP Grado Medio', 'FP_MID', 'Ciclo Formativo de Grado Medio'),
    ('FP Grado Superior', 'FP_HIGH', 'Ciclo Formativo de Grado Superior'),
    ('Grado Universitario', 'UNI_DEGREE', 'Estudios Universitarios de Grado'),
    ('Máster Universitario', 'UNI_MASTER', 'Estudios Universitarios de Postgrado');

-- 4. Tabla Estudiantes
CREATE TABLE students (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT UNIQUE,
    school_id BIGINT NOT NULL,
    first_name NVARCHAR(150) NOT NULL,
    last_name NVARCHAR(150) NOT NULL,
    invitation_email NVARCHAR(255) NOT NULL UNIQUE,
    phone NVARCHAR(50),
    motivation NVARCHAR(MAX),
    education_type_id BIGINT,
    profile_complete BIT NOT NULL DEFAULT 0,
    onboarding_complete BIT NOT NULL DEFAULT 0,
    invited_at DATETIME2,
    registered_at DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_student_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_student_school FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT FK_student_edu_type FOREIGN KEY (education_type_id) REFERENCES education_types(id)
);

CREATE INDEX idx_student_user ON students (user_id);
CREATE INDEX idx_student_school ON students (school_id);
CREATE INDEX idx_student_edu_type ON students (education_type_id);

-- 5. Preferencias de Países del Estudiante
CREATE TABLE student_country_preferences (
    student_id BIGINT NOT NULL,
    country_code NVARCHAR(100) NOT NULL,
    CONSTRAINT FK_student_country_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- 6. Documentos del Estudiante AWS (CV, Cartas, Fotos)
CREATE TABLE student_documents (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id BIGINT NOT NULL,
    document_type NVARCHAR(50) NOT NULL, -- CV, COVER_LETTER, PHOTO
    s3_key NVARCHAR(500) NOT NULL,
    original_filename NVARCHAR(255),
    file_size_bytes BIGINT,
    content_type NVARCHAR(100),
    uploaded_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_doc_student FOREIGN KEY (student_id) REFERENCES students(id)
);

CREATE INDEX idx_doc_student ON student_documents (student_id);
CREATE INDEX idx_doc_type ON student_documents (document_type);

-- 7. Diccionarios de Competencias (Categorias)
CREATE TABLE keyword_categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL UNIQUE
);

INSERT INTO keyword_categories (name) VALUES 
    ('Tecnología y Software'), 
    ('Marketing y Comunicación'), 
    ('Diseño y Creatividad'), 
    ('Empresa y Finanzas'), 
    ('Idiomas');

-- 8. Diccionario Competencias (Keywords)
CREATE TABLE keywords (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL UNIQUE,
    category_id BIGINT NOT NULL,
    CONSTRAINT FK_keyword_category FOREIGN KEY (category_id) REFERENCES keyword_categories(id)
);

CREATE INDEX idx_keyword_category ON keywords (category_id);

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
    CONSTRAINT PK_student_keywords PRIMARY KEY (student_id, keyword_id),
    CONSTRAINT FK_student_kw_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT FK_student_kw_keyword FOREIGN KEY (keyword_id) REFERENCES keywords(id)
);

-- 10. Tabla Empresas (Company)
CREATE TABLE companies (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name NVARCHAR(255) NOT NULL,
    country NVARCHAR(100) NOT NULL,
    city NVARCHAR(100),
    website NVARCHAR(255),
    description NVARCHAR(MAX),
    company_size NVARCHAR(100),
    sector NVARCHAR(100),
    logo_s3_key NVARCHAR(500),
    status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason NVARCHAR(MAX),
    approved_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_company_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_company_user ON companies (user_id);
CREATE INDEX idx_company_status ON companies (status);

-- 11. Ofertas Postuladas (Opportunity)
CREATE TABLE opportunities (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    company_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NOT NULL,
    country NVARCHAR(100) NOT NULL,
    city NVARCHAR(100),
    start_date DATE,
    end_date DATE,
    spots INT NOT NULL DEFAULT 1,
    spots_available INT NOT NULL DEFAULT 1,
    education_type_id BIGINT,
    status NVARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PENDING_REVIEW, APPROVED, CLOSED
    rejection_reason NVARCHAR(MAX),
    approved_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_opp_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT FK_opp_edu_type FOREIGN KEY (education_type_id) REFERENCES education_types(id)
);

CREATE INDEX idx_opp_company ON opportunities (company_id);
CREATE INDEX idx_opp_status ON opportunities (status);
CREATE INDEX idx_opp_edu_type ON opportunities (education_type_id);
CREATE INDEX idx_opp_country ON opportunities (country);

-- 12. Asociación N a N Oferta > Keywords requeridas
CREATE TABLE opportunity_keywords (
    opportunity_id BIGINT NOT NULL,
    keyword_id BIGINT NOT NULL,
    CONSTRAINT PK_opportunity_keywords PRIMARY KEY (opportunity_id, keyword_id),
    CONSTRAINT FK_opp_kw_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id),
    CONSTRAINT FK_opp_kw_keyword FOREIGN KEY (keyword_id) REFERENCES keywords(id)
);

-- 13. Solicitudes emitidas por estudiantes en 1 Oferta (Applications)
CREATE TABLE applications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    stage INT NOT NULL DEFAULT 1,
    status NVARCHAR(50) NOT NULL DEFAULT 'APPLIED', -- APPLIED, INTERESTED, OFFERED, ADMIN_APPROVED, CONFIRMED, EXPIRED...
    rejection_reason NVARCHAR(MAX),
    notes NVARCHAR(MAX),
    expires_at DATETIME2 NULL,
    applied_at DATETIME2 NULL,
    status_updated_at DATETIME2 NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_app_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT FK_app_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id)
);

CREATE INDEX idx_app_student ON applications (student_id);
CREATE INDEX idx_app_opportunity ON applications (opportunity_id);
CREATE INDEX idx_app_status ON applications (status);
CREATE INDEX idx_app_stage ON applications (stage);

-- 14. Entrevistas (Interview) (1 a 1 de una Application)
CREATE TABLE interviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE,
    scheduled_at DATETIME2 NOT NULL,
    video_call_link NVARCHAR(500),
    notes NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_interview_app FOREIGN KEY (application_id) REFERENCES applications(id)
);

-- 15. Inserción de Admin por defecto
INSERT INTO users (auth0_id, email, role, status) VALUES 
    ('auth0|admin-beducation', 'admin@beducation.com', 'ADMIN', 'ACTIVE');

