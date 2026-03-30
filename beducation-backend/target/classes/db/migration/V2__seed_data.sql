-- ============================================================
-- MIGRACIÓN ROBUSTA: V2__seed_data.sql
-- ============================================================
-- Esta versión asegura que los datos base existan y usa
-- búsquedas por nombre para evitar fallos de claves foráneas.
-- ============================================================

-- 1. Asegurar Categorías y Keywords Base (por si V1 no las insertó o se borraron)
IF NOT EXISTS (SELECT 1 FROM keyword_categories WHERE name = 'Tecnología y Software')
BEGIN
    INSERT INTO keyword_categories (name) VALUES 
    ('Tecnología y Software'), ('Marketing y Comunicación'), ('Diseño y Creatividad'), ('Empresa y Finanzas'), ('Idiomas');
END

IF NOT EXISTS (SELECT 1 FROM keywords WHERE name = 'Java')
BEGIN
    DECLARE @CatTech BIGINT = (SELECT id FROM keyword_categories WHERE name = 'Tecnología y Software');
    DECLARE @CatMkt BIGINT = (SELECT id FROM keyword_categories WHERE name = 'Marketing y Comunicación');
    DECLARE @CatData BIGINT = (SELECT id FROM keyword_categories WHERE name = 'Empresa y Finanzas');
    
    INSERT INTO keywords (name, category_id) VALUES 
    ('Java', @CatTech), ('Spring Boot', @CatTech), ('Vue.js', @CatTech), ('Python', @CatTech),
    ('SEO', @CatMkt), ('Social Media', @CatMkt),
    ('Analista de datos', @CatData);
END

-- 2. Insertar Empresa de Prueba
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'talent@techcorp.com')
BEGIN
    INSERT INTO users (auth0_id, email, role, status)
    VALUES ('auth0|mock-company-123', 'talent@techcorp.com', 'COMPANY', 'ACTIVE');
    
    DECLARE @UserId BIGINT = (SELECT id FROM users WHERE email = 'talent@techcorp.com');
    
    INSERT INTO companies (user_id, name, country, city, website, description, company_size, sector, status)
    VALUES (@UserId, 'TechCorp Solutions', 'Germany', 'Berlin', 'https://techcorp.example.com', 
            'Líder en soluciones de software empresarial.', 'LARGE', 'Tecnología', 'APPROVED');
END

-- 3. Insertar Ofertas
IF NOT EXISTS (SELECT 1 FROM opportunities WHERE title = 'Full Stack Developer Intern')
BEGIN
    DECLARE @CompId BIGINT = (SELECT id FROM companies WHERE name = 'TechCorp Solutions');
    
    -- Oferta 1: Full Stack
    INSERT INTO opportunities (company_id, title, description, country, city, spots, spots_available, status)
    VALUES (@CompId, 'Full Stack Developer Intern', 'Desarrollo con Java y Vue.js.', 'DE', 'Berlin', 2, 2, 'APPROVED');
    DECLARE @O1 BIGINT = SCOPE_IDENTITY();
    
    -- Oferta 2: Marketing
    INSERT INTO opportunities (company_id, title, description, country, city, spots, spots_available, status)
    VALUES (@CompId, 'Digital Marketing Assistant', 'Estrategias SEO/SEM.', 'IE', 'Dublin', 1, 1, 'APPROVED');
    DECLARE @O2 BIGINT = SCOPE_IDENTITY();

    -- Vincular Keywords por nombre
    DECLARE @KwJava BIGINT = (SELECT id FROM keywords WHERE name = 'Java');
    DECLARE @KwSpring BIGINT = (SELECT id FROM keywords WHERE name = 'Spring Boot');
    DECLARE @KwVue BIGINT = (SELECT id FROM keywords WHERE name = 'Vue.js');
    DECLARE @KwSEO BIGINT = (SELECT id FROM keywords WHERE name = 'SEO');

    IF (@KwJava IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O1, @KwJava);
    IF (@KwSpring IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O1, @KwSpring);
    IF (@KwVue IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O1, @KwVue);
    IF (@KwSEO IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O2, @KwSEO);

    -- 4. Crear solicitudes para el Estudiante 20 (Unax)
    -- Buscamos al estudiante por nombre si el ID 20 no existiera por alguna razón
    DECLARE @StudId BIGINT = (SELECT id FROM students WHERE first_name = 'UNAX' OR id = 20);
    
    IF (@StudId IS NOT NULL)
    BEGIN
        -- Evitar duplicados en aplicaciones
        IF NOT EXISTS (SELECT 1 FROM applications WHERE student_id = @StudId AND opportunity_id = @O1)
            INSERT INTO applications (student_id, opportunity_id, stage, status, applied_at, created_at)
            VALUES (@StudId, @O1, 1, 'APPLIED', GETDATE(), GETDATE());
        
        IF NOT EXISTS (SELECT 1 FROM applications WHERE student_id = @StudId AND opportunity_id = @O2)
            INSERT INTO applications (student_id, opportunity_id, stage, status, applied_at, created_at)
            VALUES (@StudId, @O2, 2, 'INTERESTED', GETDATE(), GETDATE());
    END
END
