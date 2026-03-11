-- ============================================================
--  BEDUCATION PLATFORM MVP
--  SQL Server (T-SQL) - Azure Data Studio / SSMS
--  Run this script inside the 'beducation' database
-- ============================================================

USE beducation;
GO

-- ============================================================
--  1. LOOKUP / REFERENCE TABLES
-- ============================================================

-- Education types based on Spanish FP (Formación Profesional)
CREATE TABLE Education_Type (
    education_type_id   UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    family_name         NVARCHAR(100)       NOT NULL,   -- e.g. 'Informática y Comunicaciones'
    degree_name         NVARCHAR(200)       NOT NULL,   -- e.g. 'Desarrollo de Aplicaciones Web'
    level               NVARCHAR(50)        NOT NULL,   -- e.g. 'Grado Superior', 'Grado Medio'
    is_active           BIT                 NOT NULL DEFAULT 1,
    CONSTRAINT PK_Education_Type PRIMARY KEY (education_type_id)
);
GO

-- Keywords / skills managed by admin
CREATE TABLE Keyword (
    keyword_id      UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    name            NVARCHAR(100)       NOT NULL,   -- e.g. 'JavaScript', 'AutoCAD'
    category        NVARCHAR(100)       NOT NULL,   -- e.g. 'Programming', 'Design'
    is_active       BIT                 NOT NULL DEFAULT 1,
    CONSTRAINT PK_Keyword PRIMARY KEY (keyword_id),
    CONSTRAINT UQ_Keyword_Name UNIQUE (name)
);
GO

-- ============================================================
--  2. CORE USER TABLE
-- ============================================================

CREATE TABLE [User] (
    user_id         UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    name            NVARCHAR(150)       NOT NULL,
    email           NVARCHAR(255)       NOT NULL,
    tel             NVARCHAR(30)        NULL,
    auth0_id        NVARCHAR(255)       NOT NULL,   -- Auth0 external identifier
    is_active       BIT                 NOT NULL DEFAULT 1,
    user_type       NVARCHAR(20)        NOT NULL,   -- 'admin','school','student','company'
    created_at      DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_User PRIMARY KEY (user_id),
    CONSTRAINT UQ_User_Email UNIQUE (email),
    CONSTRAINT UQ_User_Auth0 UNIQUE (auth0_id),
    CONSTRAINT CK_User_Type CHECK (user_type IN ('admin','school','student','company'))
);
GO

-- ============================================================
--  3. ROLE-SPECIFIC PROFILE TABLES
-- ============================================================

CREATE TABLE Admin (
    admin_id    UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    user_id     UNIQUEIDENTIFIER    NOT NULL,
    CONSTRAINT PK_Admin PRIMARY KEY (admin_id),
    CONSTRAINT FK_Admin_User FOREIGN KEY (user_id) REFERENCES [User](user_id)
);
GO

CREATE TABLE School (
    school_id           UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    user_id             UNIQUEIDENTIFIER    NOT NULL,
    country             NVARCHAR(100)       NOT NULL,
    city                NVARCHAR(100)       NOT NULL,
    address             NVARCHAR(255)       NULL,
    description         NVARCHAR(MAX)       NULL,
    approval_status     NVARCHAR(20)        NOT NULL DEFAULT 'pending',
    approved_at         DATETIME2           NULL,
    CONSTRAINT PK_School PRIMARY KEY (school_id),
    CONSTRAINT FK_School_User FOREIGN KEY (user_id) REFERENCES [User](user_id),
    CONSTRAINT CK_School_Status CHECK (approval_status IN ('pending','approved','rejected','suspended'))
);
GO

CREATE TABLE Company (
    company_id          UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    user_id             UNIQUEIDENTIFIER    NOT NULL,
    country             NVARCHAR(100)       NOT NULL,
    city                NVARCHAR(100)       NOT NULL,
    website             NVARCHAR(255)       NULL,
    linkedin_url        NVARCHAR(255)       NULL,
    industry_category   NVARCHAR(100)       NULL,
    description         NVARCHAR(MAX)       NULL,
    size_range          NVARCHAR(30)        NULL,   -- e.g. '1-10', '11-50', '51-200'
    approval_status     NVARCHAR(20)        NOT NULL DEFAULT 'pending',
    approved_at         DATETIME2           NULL,
    CONSTRAINT PK_Company PRIMARY KEY (company_id),
    CONSTRAINT FK_Company_User FOREIGN KEY (user_id) REFERENCES [User](user_id),
    CONSTRAINT CK_Company_Status CHECK (approval_status IN ('pending','approved','rejected','suspended'))
);
GO

CREATE TABLE Student (
    student_id          UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    user_id             UNIQUEIDENTIFIER    NOT NULL,
    school_id           UNIQUEIDENTIFIER    NOT NULL,
    education_type_id   UNIQUEIDENTIFIER    NOT NULL,
    graduation_year     INT                 NULL,
    statement           NVARCHAR(MAX)       NULL,   -- motivation statement
    profile_complete    BIT                 NOT NULL DEFAULT 0,
    created_at          DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Student PRIMARY KEY (student_id),
    CONSTRAINT FK_Student_User FOREIGN KEY (user_id) REFERENCES [User](user_id),
    CONSTRAINT FK_Student_School FOREIGN KEY (school_id) REFERENCES School(school_id),
    CONSTRAINT FK_Student_EducationType FOREIGN KEY (education_type_id) REFERENCES Education_Type(education_type_id)
);
GO

-- ============================================================
--  4. JUNCTION TABLES FOR STUDENT
-- ============================================================

-- Up to 5 country preferences per student
CREATE TABLE Country_Preference (
    student_id      UNIQUEIDENTIFIER    NOT NULL,
    country_code    CHAR(2)             NOT NULL,   -- ISO 3166-1 alpha-2
    priority_order  INT                 NOT NULL,   -- 1 = most preferred
    CONSTRAINT PK_Country_Preference PRIMARY KEY (student_id, country_code),
    CONSTRAINT FK_CountryPref_Student FOREIGN KEY (student_id) REFERENCES Student(student_id),
    CONSTRAINT CK_CountryPref_Priority CHECK (priority_order BETWEEN 1 AND 5)
);
GO

-- Up to 20 keywords per student
CREATE TABLE Student_Keyword (
    student_id      UNIQUEIDENTIFIER    NOT NULL,
    keyword_id      UNIQUEIDENTIFIER    NOT NULL,
    assigned_at     DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Student_Keyword PRIMARY KEY (student_id, keyword_id),
    CONSTRAINT FK_StudentKw_Student FOREIGN KEY (student_id) REFERENCES Student(student_id),
    CONSTRAINT FK_StudentKw_Keyword FOREIGN KEY (keyword_id) REFERENCES Keyword(keyword_id)
);
GO

-- ============================================================
--  5. OPPORTUNITY
-- ============================================================

CREATE TABLE Opportunity (
    opportunity_id      UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    company_id          UNIQUEIDENTIFIER    NOT NULL,
    education_type_id   UNIQUEIDENTIFIER    NOT NULL,
    title               NVARCHAR(200)       NOT NULL,
    description         NVARCHAR(MAX)       NULL,
    duration_months     INT                 NOT NULL,
    start_date          DATE                NULL,
    end_date            DATE                NULL,
    country             NVARCHAR(100)       NOT NULL,
    city                NVARCHAR(100)       NOT NULL,
    capacity            INT                 NOT NULL DEFAULT 1,
    willingness_score   INT                 NULL,   -- 1-5, company's interest level
    status              NVARCHAR(20)        NOT NULL DEFAULT 'draft',
    created_at          DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Opportunity PRIMARY KEY (opportunity_id),
    CONSTRAINT FK_Opp_Company FOREIGN KEY (company_id) REFERENCES Company(company_id),
    CONSTRAINT FK_Opp_EducationType FOREIGN KEY (education_type_id) REFERENCES Education_Type(education_type_id),
    CONSTRAINT CK_Opp_Status CHECK (status IN ('draft','pending_review','approved','rejected','closed')),
    CONSTRAINT CK_Opp_Willingness CHECK (willingness_score BETWEEN 1 AND 5)
);
GO

-- Up to 10 keywords per opportunity
CREATE TABLE Opportunity_Keyword (
    opportunity_id  UNIQUEIDENTIFIER    NOT NULL,
    keyword_id      UNIQUEIDENTIFIER    NOT NULL,
    CONSTRAINT PK_Opportunity_Keyword PRIMARY KEY (opportunity_id, keyword_id),
    CONSTRAINT FK_OppKw_Opportunity FOREIGN KEY (opportunity_id) REFERENCES Opportunity(opportunity_id),
    CONSTRAINT FK_OppKw_Keyword FOREIGN KEY (keyword_id) REFERENCES Keyword(keyword_id)
);
GO

-- ============================================================
--  6. APPLICATION (core workflow entity)
-- ============================================================

CREATE TABLE Application (
    application_id      UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    opportunity_id      UNIQUEIDENTIFIER    NOT NULL,
    student_id          UNIQUEIDENTIFIER    NOT NULL,
    stage               INT                 NOT NULL DEFAULT 1,   -- 1 to 5
    status              NVARCHAR(20)        NOT NULL DEFAULT 'active',
    rejection_reason    NVARCHAR(MAX)       NULL,
    created_at          DATETIME2           NOT NULL DEFAULT GETDATE(),
    updated_at          DATETIME2           NOT NULL DEFAULT GETDATE(),
    expires_at          DATETIME2           NOT NULL,             -- 30 days from created_at
    CONSTRAINT PK_Application PRIMARY KEY (application_id),
    CONSTRAINT FK_App_Opportunity FOREIGN KEY (opportunity_id) REFERENCES Opportunity(opportunity_id),
    CONSTRAINT FK_App_Student FOREIGN KEY (student_id) REFERENCES Student(student_id),
    CONSTRAINT CK_App_Stage CHECK (stage BETWEEN 1 AND 5),
    CONSTRAINT CK_App_Status CHECK (status IN ('active','withdrawn','rejected','expired','confirmed','cancelled'))
);
GO

-- ============================================================
--  7. INTERVIEW (optional step in stage 2)
-- ============================================================

CREATE TABLE Interview (
    interview_id            UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    application_id          UNIQUEIDENTIFIER    NOT NULL,
    scheduled_at            DATETIME2           NOT NULL,
    video_call_link         NVARCHAR(500)       NULL,
    preparation_instructions NVARCHAR(MAX)      NULL,
    created_at              DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Interview PRIMARY KEY (interview_id),
    CONSTRAINT FK_Interview_Application FOREIGN KEY (application_id) REFERENCES Application(application_id)
);
GO

-- ============================================================
--  8. PLACEMENT OFFER (stages 3-5)
-- ============================================================

CREATE TABLE Placement_Offer (
    placement_offer_id  UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    application_id      UNIQUEIDENTIFIER    NOT NULL,
    admin_approved_by   UNIQUEIDENTIFIER    NULL,   -- FK to Admin
    school_approved_by  UNIQUEIDENTIFIER    NULL,   -- FK to School
    student_response    NVARCHAR(20)        NOT NULL DEFAULT 'pending',
    admin_status        NVARCHAR(20)        NOT NULL DEFAULT 'pending',
    school_status       NVARCHAR(20)        NOT NULL DEFAULT 'pending',
    confirmed_at        DATETIME2           NULL,
    created_at          DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Placement_Offer PRIMARY KEY (placement_offer_id),
    CONSTRAINT FK_PlacOffer_Application FOREIGN KEY (application_id) REFERENCES Application(application_id),
    CONSTRAINT FK_PlacOffer_Admin FOREIGN KEY (admin_approved_by) REFERENCES Admin(admin_id),
    CONSTRAINT FK_PlacOffer_School FOREIGN KEY (school_approved_by) REFERENCES School(school_id),
    CONSTRAINT CK_PlacOffer_StudentResp CHECK (student_response IN ('pending','accepted','declined')),
    CONSTRAINT CK_PlacOffer_AdminStatus CHECK (admin_status IN ('pending','approved','rejected')),
    CONSTRAINT CK_PlacOffer_SchoolStatus CHECK (school_status IN ('pending','approved','rejected'))
);
GO

-- ============================================================
--  9. DOCUMENT (S3-backed, polymorphic)
-- ============================================================

CREATE TABLE Document (
    document_id     UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    owner_id        UNIQUEIDENTIFIER    NOT NULL,   -- student_id, company_id, or placement_offer_id
    owner_type      NVARCHAR(20)        NOT NULL,   -- 'student', 'company', 'placement'
    doc_type        NVARCHAR(30)        NOT NULL,   -- 'cv', 'cover_letter', 'photo', 'logo', 'learning_agreement'
    s3_key          NVARCHAR(500)       NOT NULL,   -- path in AWS S3
    filename        NVARCHAR(255)       NOT NULL,
    mime_type       NVARCHAR(100)       NOT NULL,
    version         INT                 NOT NULL DEFAULT 1,   -- versioning for learning agreements
    uploaded_at     DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Document PRIMARY KEY (document_id),
    CONSTRAINT CK_Document_OwnerType CHECK (owner_type IN ('student','company','placement')),
    CONSTRAINT CK_Document_DocType CHECK (doc_type IN ('cv','cover_letter','photo','logo','learning_agreement'))
);
GO

-- ============================================================
--  10. NOTIFICATION (email event log)
-- ============================================================

CREATE TABLE Notification (
    notification_id     UNIQUEIDENTIFIER    NOT NULL DEFAULT NEWID(),
    recipient_id        UNIQUEIDENTIFIER    NOT NULL,   -- user_id
    recipient_type      NVARCHAR(20)        NOT NULL,   -- 'admin','school','student','company'
    event_type          NVARCHAR(60)        NOT NULL,   -- e.g. 'offer_made', 'placement_confirmed'
    status              NVARCHAR(20)        NOT NULL DEFAULT 'sent',
    sent_at             DATETIME2           NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Notification PRIMARY KEY (notification_id),
    CONSTRAINT CK_Notif_RecipType CHECK (recipient_type IN ('admin','school','student','company')),
    CONSTRAINT CK_Notif_Status CHECK (status IN ('sent','failed','pending'))
);
GO

-- ============================================================
--  INDEXES (performance)
-- ============================================================

CREATE INDEX IX_Application_Student     ON Application(student_id);
CREATE INDEX IX_Application_Opportunity ON Application(opportunity_id);
CREATE INDEX IX_Opportunity_Company     ON Opportunity(company_id);
CREATE INDEX IX_Student_School          ON Student(school_id);
CREATE INDEX IX_Document_Owner         ON Document(owner_id, owner_type);
CREATE INDEX IX_Notification_Recipient ON Notification(recipient_id);
GO

-- ============================================================
--  SAMPLE DATA - Education Types (FP Spain, subset)
-- ============================================================

INSERT INTO Education_Type (family_name, degree_name, level) VALUES
('Informática y Comunicaciones', 'Desarrollo de Aplicaciones Web', 'Grado Superior'),
('Informática y Comunicaciones', 'Desarrollo de Aplicaciones Multiplataforma', 'Grado Superior'),
('Informática y Comunicaciones', 'Administración de Sistemas Informáticos en Red', 'Grado Superior'),
('Informática y Comunicaciones', 'Sistemas Microinformáticos y Redes', 'Grado Medio'),
('Administración y Gestión', 'Administración y Finanzas', 'Grado Superior'),
('Administración y Gestión', 'Asistencia a la Dirección', 'Grado Superior'),
('Comercio y Marketing', 'Marketing y Publicidad', 'Grado Superior'),
('Comercio y Marketing', 'Gestión de Ventas y Espacios Comerciales', 'Grado Superior'),
('Sanidad', 'Imagen para el Diagnóstico y Medicina Nuclear', 'Grado Superior'),
('Hostelería y Turismo', 'Agencias de Viajes y Gestión de Eventos', 'Grado Superior');
GO

-- ============================================================
--  SAMPLE DATA - Keywords
-- ============================================================

INSERT INTO Keyword (name, category) VALUES
('JavaScript', 'Programming'),
('Python', 'Programming'),
('SQL', 'Programming'),
('React', 'Frontend'),
('Node.js', 'Backend'),
('AutoCAD', 'Design'),
('Photoshop', 'Design'),
('Excel', 'Office'),
('Marketing Digital', 'Marketing'),
('SEO', 'Marketing'),
('Contabilidad', 'Finance'),
('Atención al Cliente', 'Soft Skills'),
('Trabajo en Equipo', 'Soft Skills'),
('Inglés B2', 'Languages'),
('Alemán B1', 'Languages');
GO

PRINT 'Beducation database created successfully.';
GO