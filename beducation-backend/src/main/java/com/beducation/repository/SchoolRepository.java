package com.beducation.repository;

import com.beducation.model.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Repositorio: SchoolRepository
 * ============================================================
 * Acceso a datos de escuelas. Incluye métodos de búsqueda
 * por estado para el panel de administración.
 * ============================================================
 */
@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    /**
     * Encuentra la escuela asociada a un usuario concreto.
     * @param userId ID del usuario en la tabla users
     * @return la escuela del usuario, si existe
     */
    Optional<School> findByUserId(Long userId);

    /**
     * Lista escuelas paginadas por estado.
     * Usado en el panel de admin (lista de pendientes, aprobadas, etc.)
     * @param status estado de aprobación
     * @param pageable opciones de paginación/ordenación
     * @return página de escuelas con ese estado
     */
    Page<School> findByStatus(School.ApprovalStatus status, Pageable pageable);

    /**
     * Cuenta las escuelas con un estado específico.
     * Usado en las estadísticas del panel de administración.
     */
    long countByStatus(School.ApprovalStatus status);
}
