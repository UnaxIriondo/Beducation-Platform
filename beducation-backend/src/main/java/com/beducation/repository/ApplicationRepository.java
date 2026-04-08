package com.beducation.repository;

import com.beducation.model.Application;
import com.beducation.model.Application.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Repositorio: ApplicationRepository
 * ============================================================
 * Acceso a la tabla de solicitudes — núcleo del sistema de
 * workflow de 5 etapas.
 *
 * Incluye consultas JPQL para operaciones específicas del
 * flujo de trabajo que no se pueden expresar solo con el
 * nombrado de métodos de Spring Data.
 * ============================================================
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Obtiene todas las solicitudes de un estudiante, paginadas.
     * Usado en el "Seguimiento de Solicitudes" del portal de estudiante.
     */
    Page<Application> findByStudentId(Long studentId, Pageable pageable);

    /**
     * Obtiene solicitudes filtradas por su estado.
     */
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * Obtiene todas las solicitudes para una oferta concreta.
     * Usado por la empresa para revisar candidatos.
     */
    Page<Application> findByOpportunityId(Long opportunityId, Pageable pageable);

    /**
     * Comprueba si un estudiante ya ha aplicado a una oferta.
     * Evita solicitudes duplicadas en el mismo proceso.
     */
    boolean existsByStudentIdAndOpportunityId(Long studentId, Long opportunityId);

    /**
     * Busca solicitudes activas que han superado su fecha de expiración.
     * Ejecutado periódicamente por el scheduler de expiración.
     * Solo afecta solicitudes en etapas 1-3 (no confirmadas ni rechazadas).
     */
    @Query("SELECT a FROM Application a WHERE a.expiresAt < :now " +
           "AND a.status NOT IN ('CONFIRMED', 'REJECTED', 'WITHDRAWN', 'EXPIRED', 'OFFER_REJECTED')")
    List<Application> findExpiredApplications(@Param("now") LocalDateTime now);

    /**
     * Cancela todas las otras solicitudes de un estudiante cuando confirma un placement.
     * Regla de negocio: al confirmar prácticas, el estudiante
     * no puede continuar en otros procesos.
     *
     * @param studentId   ID del estudiante
     * @param excludedId  ID de la solicitud que se está confirmando (no cancelar)
     */
    @Modifying
    @Query("UPDATE Application a SET a.status = 'WITHDRAWN' " +
           "WHERE a.student.id = :studentId AND a.id != :excludedId " +
           "AND a.status NOT IN ('CONFIRMED', 'REJECTED', 'WITHDRAWN', 'EXPIRED', 'OFFER_REJECTED')")
    void cancelOtherApplications(@Param("studentId") Long studentId,
                                  @Param("excludedId") Long excludedId);

    /**
     * Solicitudes en Etapa 4 (ADMIN_APPROVED) pendientes de aprobación
     * de la escuela — para el panel de admin.
     */
    @Query("SELECT a FROM Application a WHERE a.status = 'ADMIN_APPROVED'")
    Page<Application> findPendingSchoolApprovals(Pageable pageable);

    /**
     * Solicitudes en Etapa 4 (ADMIN_APPROVED) para una escuela específica.
     */
    @Query("SELECT a FROM Application a WHERE a.status = 'ADMIN_APPROVED' AND a.student.school.id = :schoolId")
    Page<Application> findPendingSchoolApprovalsBySchoolId(@Param("schoolId") Long schoolId, Pageable pageable);

    /**
     * Estadísticas: cuenta solicitudes por estado.
     * Usado en el informe del panel de administración.
     */
    long countByStatus(ApplicationStatus status);

    /**
     * Estadísticas por oferta: cuenta solicitudes activas.
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.opportunity.id = :opportunityId " +
           "AND a.status NOT IN ('WITHDRAWN', 'EXPIRED', 'REJECTED', 'OFFER_REJECTED')")
    long countActiveByOpportunityId(@Param("opportunityId") Long opportunityId);
}
