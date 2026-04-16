-- ============================================================
-- MIGRACIÓN ROBUSTA: V2__seed_data.sql (VERSIÓN LIMPIA)
-- ============================================================
-- Solo inserta categorías y palabras clave básicas.
-- NO inserta usuarios, empresas ni ofertas de prueba.
-- ============================================================

-- 1. Asegurar Categorías y Keywords Base
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

-- Fin de migración limpia.
