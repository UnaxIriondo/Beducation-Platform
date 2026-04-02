-- ============================================================
-- V4__add_passwords.sql
-- ============================================================
-- Añade la capacidad de iniciar sesión mediante contraseñas
-- locales a la BD para la funcionalidad de estudiantes híbrida.
-- ============================================================

-- 1. Añadimos la contraseña consolidada al usuario
ALTER TABLE users ADD password VARCHAR(255);

-- 2. Eliminar el índice manual que depende de auth0_id
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_user_auth0' AND object_id = OBJECT_ID('users'))
BEGIN
    DROP INDEX idx_user_auth0 ON users;
END

-- 3. Eliminar la constraint UNIQUE que depende de auth0_id (SQL Server genera nombres como UK_...)
DECLARE @ConstraintName nvarchar(200);
SELECT @ConstraintName = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic ON kc.parent_object_id = ic.object_id AND kc.unique_index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE kc.type = 'UQ' AND kc.parent_object_id = OBJECT_ID('users') AND c.name = 'auth0_id';

IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE users DROP CONSTRAINT ' + @ConstraintName);
END

-- 4. Hacemos que auth0_id sea NULLABLE
ALTER TABLE users ALTER COLUMN auth0_id NVARCHAR(255) NULL;

-- 5. Recreamos el índice como un índice filtrado para permitir múltiples NULLs pero emails únicos si no es NULL
CREATE UNIQUE INDEX idx_user_auth0 ON users(auth0_id) WHERE auth0_id IS NOT NULL;

-- 6. Añadimos la contraseña temporal al estudiante (para el hash de invitación)
ALTER TABLE students ADD temp_password VARCHAR(255);
