package com.beducation.repository;

import com.beducation.model.GalleryAccessRequest;
import com.beducation.model.GalleryAccessRequest.AccessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * Repositorio: GalleryAccessRequestRepository
 * ============================================================
 * Gestión de persistencia para solicitudes de acceso a galería.
 * ============================================================
 */
@Repository
public interface GalleryAccessRequestRepository extends JpaRepository<GalleryAccessRequest, Long> {

    /**
     * Busca la solicitud más reciente de un usuario.
     */
    Optional<GalleryAccessRequest> findFirstByUserIdOrderByRequestedAtDesc(Long userId);

    /**
     * Obtiene solicitudes por estado para el panel de admin.
     */
    Page<GalleryAccessRequest> findByStatus(AccessStatus status, Pageable pageable);

    /**
     * Cuenta el número de solicitudes en un estado determinado.
     */
    long countByStatus(AccessStatus status);

    /**
     * Verifica si un usuario ya tiene una solicitud aprobada.
     */
    boolean existsByUserIdAndStatus(Long userId, AccessStatus status);
}
