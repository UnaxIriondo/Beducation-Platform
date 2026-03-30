package com.beducation.repository;

import com.beducation.model.Opportunity;
import com.beducation.model.Opportunity.OpportunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================
 * Repositorio: OpportunityRepository
 * ============================================================
 * Acceso a ofertas de prácticas. Incluye métodos de búsqueda
 * con filtros para el portal de estudiantes y el admin.
 * ============================================================
 */
@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Lista las ofertas de una empresa específica, paginadas.
     * Usado en el panel de la empresa para ver sus ofertas.
     */
    Page<Opportunity> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Lista ofertas aprobadas con filtros opcionales.
     * Usado en el portal de estudiante para buscar oportunidades.
     * Todos los parámetros son opcionales (null = sin filtro).
     *
     * @param country     filtrar por país (o null para todos)
     * @param educTypeId  filtrar por tipo de formación (o null para todos)
     * @param keyword     búsqueda en título o descripción
     * @param pageable    paginación
     */
    @Query("SELECT o FROM Opportunity o " +
           "WHERE o.status = 'APPROVED' " +
           "AND o.spotsAvailable > 0 " +
           "AND (:country IS NULL OR o.country = :country) " +
           "AND (:educTypeId IS NULL OR (o.educationType IS NOT NULL AND o.educationType.id = :educTypeId)) " +
           "AND (:keyword IS NULL OR (o.title LIKE CONCAT('%', :keyword, '%') OR " +
           "     o.description LIKE CONCAT('%', :keyword, '%') OR " +
           "     EXISTS (SELECT 1 FROM o.keywords k WHERE k.name LIKE CONCAT('%', :keyword, '%'))))")
    Page<Opportunity> searchApprovedOpportunities(
        @Param("country") String country,
        @Param("educTypeId") Long educTypeId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * Lista ofertas pendientes de revisión por el admin.
     */
    Page<Opportunity> findByStatus(OpportunityStatus status, Pageable pageable);

    /**
     * Obtiene las ofertas más compatibles para el algoritmo de matching.
     * Devuelve ofertas APPROVED que tienen plazas disponibles.
     * El scoring se calcula en Java (MatchingService) no en SQL.
     */
    @Query("SELECT o FROM Opportunity o WHERE o.status = 'APPROVED' AND o.spotsAvailable > 0")
    List<Opportunity> findAllApprovedWithSpots();

    /**
     * Estadísticas: cuenta ofertas por estado.
     */
    long countByStatus(OpportunityStatus status);
}
