-- ============================================================
-- SAMPLE DATA - USERS
-- ============================================================

DECLARE @adminUserId UNIQUEIDENTIFIER = NEWID();
DECLARE @schoolUserId UNIQUEIDENTIFIER = NEWID();
DECLARE @companyUserId UNIQUEIDENTIFIER = NEWID();
DECLARE @studentUserId UNIQUEIDENTIFIER = NEWID();

INSERT INTO [User] (user_id, name, email, auth0_id, user_type)
VALUES
(@adminUserId, 'Admin Test', 'admin@test.com', 'auth0|admin1', 'admin'),
(@schoolUserId, 'IES Tecnológico', 'school@test.com', 'auth0|school1', 'school'),
(@companyUserId, 'Tech Corp', 'company@test.com', 'auth0|company1', 'company'),
(@studentUserId, 'Juan Pérez', 'student@test.com', 'auth0|student1', 'student');

-- ============================================================
-- ADMIN
-- ============================================================

DECLARE @adminId UNIQUEIDENTIFIER = NEWID();

INSERT INTO Admin (admin_id, user_id)
VALUES (@adminId, @adminUserId);

-- ============================================================
-- SCHOOL
-- ============================================================

DECLARE @schoolId UNIQUEIDENTIFIER = NEWID();

INSERT INTO School (school_id, user_id, country, city, address, approval_status, approved_at)
VALUES (@schoolId, @schoolUserId, 'Spain', 'Madrid', 'Calle Falsa 123', 'approved', GETDATE());

-- ============================================================
-- COMPANY
-- ============================================================

DECLARE @companyId UNIQUEIDENTIFIER = NEWID();

INSERT INTO Company (company_id, user_id, country, city, website, industry_category, size_range, approval_status, approved_at)
VALUES (@companyId, @companyUserId, 'Spain', 'Barcelona', 'https://techcorp.com', 'IT', '11-50', 'approved', GETDATE());

-- ============================================================
-- STUDENT
-- ============================================================

DECLARE @studentId UNIQUEIDENTIFIER = NEWID();
DECLARE @educationTypeId UNIQUEIDENTIFIER;

-- coger un education_type existente
SELECT TOP 1 @educationTypeId = education_type_id FROM Education_Type;

INSERT INTO Student (student_id, user_id, school_id, education_type_id, graduation_year, statement, profile_complete)
VALUES (@studentId, @studentUserId, @schoolId, @educationTypeId, 2025, 'Apasionado por el desarrollo web', 1);

-- ============================================================
-- STUDENT PREFERENCES
-- ============================================================

INSERT INTO Country_Preference (student_id, country_code, priority_order)
VALUES
(@studentId, 'ES', 1),
(@studentId, 'DE', 2),
(@studentId, 'FR', 3);

-- ============================================================
-- STUDENT KEYWORDS
-- ============================================================

DECLARE @kw1 UNIQUEIDENTIFIER;
DECLARE @kw2 UNIQUEIDENTIFIER;

SELECT TOP 1 @kw1 = keyword_id FROM Keyword WHERE name = 'JavaScript';
SELECT TOP 1 @kw2 = keyword_id FROM Keyword WHERE name = 'SQL';

INSERT INTO Student_Keyword (student_id, keyword_id)
VALUES
(@studentId, @kw1),
(@studentId, @kw2);

-- ============================================================
-- OPPORTUNITY
-- ============================================================

DECLARE @opportunityId UNIQUEIDENTIFIER = NEWID();

INSERT INTO Opportunity (opportunity_id, company_id, education_type_id, title, description, duration_months, country, city, capacity, status)
VALUES
(@opportunityId, @companyId, @educationTypeId, 'Frontend Developer Intern', 'Prácticas en React', 6, 'Spain', 'Barcelona', 2, 'approved');

-- ============================================================
-- OPPORTUNITY KEYWORDS
-- ============================================================

INSERT INTO Opportunity_Keyword (opportunity_id, keyword_id)
VALUES
(@opportunityId, @kw1),
(@opportunityId, @kw2);

-- ============================================================
-- APPLICATION
-- ============================================================

DECLARE @applicationId UNIQUEIDENTIFIER = NEWID();

INSERT INTO Application (application_id, opportunity_id, student_id, stage, status, expires_at)
VALUES
(@applicationId, @opportunityId, @studentId, 2, 'active', DATEADD(DAY, 30, GETDATE()));

-- ============================================================
-- INTERVIEW
-- ============================================================

INSERT INTO Interview (application_id, scheduled_at, video_call_link)
VALUES
(@applicationId, DATEADD(DAY, 5, GETDATE()), 'https://meet.test/interview');

-- ============================================================
-- PLACEMENT OFFER
-- ============================================================

DECLARE @placementId UNIQUEIDENTIFIER = NEWID();

INSERT INTO Placement_Offer (placement_offer_id, application_id, admin_approved_by, school_approved_by, student_response, admin_status, school_status)
VALUES
(@placementId, @applicationId, @adminId, @schoolId, 'pending', 'approved', 'approved');

-- ============================================================
-- DOCUMENTS
-- ============================================================

INSERT INTO Document (owner_id, owner_type, doc_type, s3_key, filename, mime_type)
VALUES
(@studentId, 'student', 'cv', 'cv/juan.pdf', 'juan_cv.pdf', 'application/pdf'),
(@companyId, 'company', 'logo', 'logo/company.png', 'logo.png', 'image/png');

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

INSERT INTO Notification (recipient_id, recipient_type, event_type)
VALUES
(@studentUserId, 'student', 'application_created'),
(@companyUserId, 'company', 'new_candidate');
