package com.beducation.repository;

import com.beducation.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Repositorio: StudentRepository
 * ============================================================
 * Acceso a datos de estudiantes con soporte para búsqueda
 * compleja (filtros por keyword y tipo de formación).
 * ============================================================
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Encuentra el estudiante vinculado a un usuario. */
    Optional<Student> findByUserId(Long userId);

    /** Busca al estudiante por el email de invitación. */
    Optional<Student> findByInvitationEmail(String invitationEmail);

    /** Verifica si ya existe una invitación para ese email. */
    boolean existsByInvitationEmail(String invitationEmail);

    /**
     * Lista todos los estudiantes de una escuela, paginados.
     * @param schoolId ID de la escuela
     */
    Page<Student> findBySchoolId(Long schoolId, Pageable pageable);

    /** Lista todos los estudiantes de la escuela sin paginación para cálculos */
    List<Student> findBySchoolId(Long schoolId);

    /**
     * Búsqueda compleja de estudiantes para el portal de empresa.
     * Permite filtrar por tipo de formación y/o keyword.
     * Solo muestra estudiantes con perfil completo.
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN s.keywords k " +
           "WHERE s.profileComplete = true " +
           "AND (:educTypeId IS NULL OR s.educationType.id = :educTypeId) " +
           "AND (:keywordName IS NULL OR LOWER(k.name) LIKE LOWER(CONCAT('%', :keywordName, '%')))")
    Page<Student> searchStudents(
        @Param("educTypeId") Long educTypeId,
        @Param("keywordName") String keywordName,
        Pageable pageable
    );

    /**
     * Cuenta todos los estudiantes de una escuela.
     * Usado en estadísticas del panel de administración.
     */
    long countBySchoolId(Long schoolId);

    /**
     * Cuenta los estudiantes con perfil completo.
     */
    long countByProfileComplete(Boolean profileComplete);
}
