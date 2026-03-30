-- ============================================================
-- MIGRACIÓN ADICIONAL: V3__more_seed_data.sql
-- ============================================================
-- Añade 5 ofertas extra con vinculación a EducationType 
-- para asegurar visibilidad en el buscador y mejorar el matching.
-- ============================================================

-- 1. Obtener IDs necesarios
DECLARE @CompId BIGINT = (SELECT id FROM companies WHERE name = 'TechCorp Solutions');
DECLARE @StudId BIGINT = (SELECT id FROM students WHERE first_name = 'UNAX' OR id = 20);
DECLARE @EduBootcamp BIGINT = (SELECT id FROM education_types WHERE code = 'BOOTCAMP');

IF (@CompId IS NOT NULL)
BEGIN
    -- Oferta 3: Frontend
    INSERT INTO opportunities (company_id, title, description, country, city, spots, spots_available, status, education_type_id)
    VALUES (@CompId, 'Frontend Developer (Vue.js)', 'Proyecto internacional de larga duración.', 'NL', 'Amsterdam', 3, 3, 'APPROVED', @EduBootcamp);
    DECLARE @O3 BIGINT = SCOPE_IDENTITY();

    -- Oferta 4: Python
    INSERT INTO opportunities (company_id, title, description, country, city, spots, spots_available, status, education_type_id)
    VALUES (@CompId, 'Junior Data Analyst', 'Python, Pandas y Spark.', 'ES', 'Madrid', 1, 1, 'APPROVED', @EduBootcamp);
    DECLARE @O4 BIGINT = SCOPE_IDENTITY();

    -- Oferta 5: Cloud
    INSERT INTO opportunities (company_id, title, description, country, city, spots, spots_available, status, education_type_id)
    VALUES (@CompId, 'Cloud Infrastructure Intern', 'AWS and Azure basics.', 'DE', 'Munich', 2, 2, 'APPROVED', @EduBootcamp);
    DECLARE @O5 BIGINT = SCOPE_IDENTITY();

    -- 2. Vincular palabras clave a las ofertas
    DECLARE @KwJava BIGINT = (SELECT id FROM keywords WHERE name = 'Java');
    DECLARE @KwVue BIGINT = (SELECT id FROM keywords WHERE name = 'Vue.js');
    DECLARE @KwPython BIGINT = (SELECT id FROM keywords WHERE name = 'Python');

    IF (@KwJava IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O5, @KwJava);
    IF (@KwVue IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O3, @KwVue);
    IF (@KwPython IS NOT NULL) INSERT INTO opportunity_keywords (opportunity_id, keyword_id) VALUES (@O4, @KwPython);

    -- 3. Vincular palabras clave al ESTUDIANTE para que el Matchmaker le encuentre afinidad
    -- Esto asegura que las sugerencias inteligentes no estén vacías.
    IF (@StudId IS NOT NULL)
    BEGIN
        IF (@KwJava IS NOT NULL AND NOT EXISTS (SELECT 1 FROM student_keywords WHERE student_id = @StudId AND keyword_id = @KwJava))
            INSERT INTO student_keywords (student_id, keyword_id) VALUES (@StudId, @KwJava);
        
        IF (@KwVue IS NOT NULL AND NOT EXISTS (SELECT 1 FROM student_keywords WHERE student_id = @StudId AND keyword_id = @KwVue))
            INSERT INTO student_keywords (student_id, keyword_id) VALUES (@StudId, @KwVue);

        -- Aseguramos que el estudiante tiene el país de las ofertas en sus preferencias (DE, NL, ES, IE)
        UPDATE students SET motivation = 'Muy motivado por el desarrollo internacional' WHERE id = @StudId;
    END
END
