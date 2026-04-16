package com.beducation.service;

import com.beducation.model.*;
import com.beducation.model.Application.ApplicationStatus;
import com.beducation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * Servicio: ApplicationService
 * ============================================================
 * Gestiona toda la lógica del flujo de trabajo de 5 etapas:
 *
 *   Etapa 1: Estudiante aplica a una oferta → APPLIED
 *   Etapa 2: Empresa expresa interés → INTERESTED
 *            Empresa programa entrevista
 *   Etapa 3: Empresa hace oferta formal → OFFERED
 *            Estudiante acepta o rechaza la oferta
 *   Etapa 4: Admin aprueba el placement → ADMIN_APPROVED
 *   Etapa 5: Escuela confirma el placement → CONFIRMED
 *            → Se cancelan automáticamente otras solicitudes
 *
 * Reglas de negocio importantes:
 *   - Solo estudiantes con perfil completo pueden aplicar
 *   - No se puede aplicar a la misma oferta dos veces
 *   - Solo se pueden retirar solicitudes en etapas 1-2
 *   - Al confirmar placement, se cancelan otras solicitudes del estudiante
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final EmailService emailService;
    private final com.beducation.util.MatchingEngine matchingEngine;

    // ── Tiempo de expiración de solicitudes sin actividad ──
    private static final int EXPIRY_DAYS = 30;

    /**
     * ETAPA 1: Un estudiante envía una solicitud a una oferta.
     *
     * Validaciones:
     *   - El estudiante debe tener perfil completo
     *   - La oferta debe estar APPROVED y tener plazas
     *   - El estudiante no puede haber aplicado antes a la misma oferta
     *
     * @param studentId     ID del estudiante que aplica
     * @param opportunityId ID de la oferta a la que aplica
     * @return la solicitud creada
     */
    public Application apply(Long studentId, Long opportunityId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + studentId));

        // Validar perfil completo
        if (!student.getProfileComplete()) {
            throw new IllegalStateException(
                "El perfil del estudiante debe estar completo para enviar solicitudes. " +
                "Falta: CV obligatorio, motivación, keywords y países de preferencia.");
        }

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada: " + opportunityId));

        // Validar que la oferta esté activa
        if (opportunity.getStatus() != Opportunity.OpportunityStatus.APPROVED) {
            throw new IllegalStateException("La oferta no está disponible para solicitudes.");
        }

        // Validar plazas disponibles
        if (opportunity.getSpotsAvailable() <= 0) {
            throw new IllegalStateException("No quedan plazas disponibles en esta oferta.");
        }

        // Validar que no haya aplicado antes
        if (applicationRepository.existsByStudentIdAndOpportunityId(studentId, opportunityId)) {
            throw new IllegalStateException("Ya has enviado una solicitud para esta oferta.");
        }

        // Calcular compatibilidad mediante el MatchingEngine
        double rawScore = matchingEngine.calculateScore(student, opportunity);
        int finalScore = (int) (rawScore * 100);
        
        List<String> commonKeywords = student.getKeywords().stream()
            .filter(sk -> opportunity.getKeywords().stream().anyMatch(ok -> ok.getId().equals(sk.getId())))
            .map(Keyword::getName)
            .collect(java.util.stream.Collectors.toList());
        String kwString = String.join(", ", commonKeywords);

        // Crear la solicitud en Etapa 1
        Application application = Application.builder()
            .student(student)
            .opportunity(opportunity)
            .stage(1)
            .status(ApplicationStatus.APPLIED)
            .compatibilityScore(finalScore)
            .matchedKeywords(kwString)
            .appliedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(EXPIRY_DAYS))
            .statusUpdatedAt(LocalDateTime.now())
            .build();

        Application saved = applicationRepository.save(application);

        // Notificar a la empresa por email
        emailService.sendApplicationReceivedEmail(opportunity.getCompany(), student, opportunity);

        log.info("Nueva solicitud creada: estudiante={} oferta={} score={} application={}",
            studentId, opportunityId, finalScore, saved.getId());

        return saved;
    }

    /**
     * ETAPA 2a: La empresa expresa interés en un candidato.
     * Transición: APPLIED → INTERESTED
     *
     * @param applicationId ID de la solicitud
     * @param companyId     ID de la empresa (para verificar ownership)
     */
    public Application expressInterest(Long applicationId, Long companyId) {
        Application application = findAndValidateForCompany(applicationId, companyId);
        validateTransition(application, ApplicationStatus.APPLIED);

        application.setStatus(ApplicationStatus.INTERESTED);
        application.setStage(2);
        application.setStatusUpdatedAt(LocalDateTime.now());
        application.setExpiresAt(LocalDateTime.now().plusDays(EXPIRY_DAYS));

        Application saved = applicationRepository.save(application);

        // Notificar al estudiante
        emailService.sendInterestExpressedEmail(application.getStudent(), application.getOpportunity());

        log.info("Empresa {} expresó interés en solicitud {}", companyId, applicationId);
        return saved;
    }

    /**
     * ETAPA 2b: La empresa programa una entrevista.
     * Solo válido cuando la solicitud está en estado INTERESTED.
     *
     * @param applicationId  ID de la solicitud
     * @param companyId      ID de la empresa (verificación)
     * @param scheduledAt    fecha/hora de la entrevista
     * @param videoCallLink  enlace de la videollamada
     * @param notes          notas adicionales (opcional)
     */
    public Application scheduleInterview(Long applicationId, Long companyId,
                                          LocalDateTime scheduledAt, String videoCallLink,
                                          String notes) {
        Application application = findAndValidateForCompany(applicationId, companyId);

        if (application.getStatus() != ApplicationStatus.INTERESTED) {
            throw new IllegalStateException(
                "Solo se puede programar entrevista cuando el candidato está en estado INTERESTED.");
        }

        // Crear o actualizar la entrevista
        Interview interview = Interview.builder()
            .application(application)
            .scheduledAt(scheduledAt)
            .videoCallLink(videoCallLink)
            .notes(notes)
            .createdAt(LocalDateTime.now())
            .build();

        application.setInterview(interview);
        application.setExpiresAt(LocalDateTime.now().plusDays(EXPIRY_DAYS));

        Application saved = applicationRepository.save(application);

        // Notificar al estudiante con los datos de la entrevista
        emailService.sendInterviewScheduledEmail(
            application.getStudent(), application.getOpportunity(), scheduledAt, videoCallLink);

        return saved;
    }

    /**
     * ETAPA 3a: La empresa realiza una oferta formal al estudiante.
     * Transición: INTERESTED → OFFERED
     */
    public Application makeOffer(Long applicationId, Long companyId) {
        Application application = findAndValidateForCompany(applicationId, companyId);
        validateTransition(application, ApplicationStatus.INTERESTED);

        application.setStatus(ApplicationStatus.OFFERED);
        application.setStage(3);
        application.setStatusUpdatedAt(LocalDateTime.now());
        application.setExpiresAt(LocalDateTime.now().plusDays(EXPIRY_DAYS));

        Application saved = applicationRepository.save(application);

        // Notificar al estudiante de la oferta
        emailService.sendOfferMadeEmail(application.getStudent(), application.getOpportunity());

        return saved;
    }

    /**
     * ETAPA 3b: El estudiante ACEPTA la oferta de la empresa.
     * Transición: OFFERED → ADMIN_APPROVED (pasa a etapa 4)
     * (En el flujo simplificado, la aceptación del estudiante
     *  mueve a etapa 4 pendiente de aprobación del admin)
     */
    public Application acceptOffer(Long applicationId, Long studentId) {
        Application application = findAndValidateForStudent(applicationId, studentId);
        validateTransition(application, ApplicationStatus.OFFERED);

        application.setStatus(ApplicationStatus.ADMIN_APPROVED);
        application.setStage(4);
        application.setStatusUpdatedAt(LocalDateTime.now());
        application.setExpiresAt(LocalDateTime.now().plusDays(EXPIRY_DAYS));

        Application saved = applicationRepository.save(application);

        // Notificar al admin para que revise el placement
        emailService.sendPlacementPendingAdminEmail(application);

        return saved;
    }

    /**
     * ETAPA 3c: El estudiante RECHAZA la oferta de la empresa.
     */
    public Application rejectOffer(Long applicationId, Long studentId) {
        Application application = findAndValidateForStudent(applicationId, studentId);
        validateTransition(application, ApplicationStatus.OFFERED);

        application.setStatus(ApplicationStatus.OFFER_REJECTED);
        application.setStatusUpdatedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        emailService.sendOfferRejectedEmail(application.getOpportunity().getCompany(), application);
        return saved;
    }

    /**
     * ETAPA 4: El Admin aprueba el placement (pre-aprobación).
     * Transición: ADMIN_APPROVED (stage 4) → notifica a la escuela
     * La solicitud ya está en ADMIN_APPROVED, el admin simplemente
     * la revisa y la escuela recibe notificación para aprobación final.
     */
    public Application adminAprovePlacement(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + applicationId));

        if (application.getStatus() != ApplicationStatus.ADMIN_APPROVED) {
            throw new IllegalStateException("La solicitud no está en estado ADMIN_APPROVED.");
        }

        // Notificar a la escuela para la aprobación final (Etapa 5)
        emailService.sendFinalApprovalRequestEmail(
            application.getStudent().getSchool(), application);

        return application;
    }

    /**
     * ETAPA 5: La Escuela da la aprobación FINAL del placement.
     * Transición: ADMIN_APPROVED → CONFIRMED
     * → Al confirmar, se cancelan automáticamente las demás solicitudes del estudiante.
     * → La oferta decrementa sus plazas disponibles.
     */
    public Application schoolAprovePlacement(Long applicationId, Long schoolId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + applicationId));

        // Verificar que la escuela que aprueba es la escuela del estudiante
        if (!application.getStudent().getSchool().getId().equals(schoolId)) {
            throw new AccessDeniedException("Esta escuela no puede aprobar esta solicitud.");
        }

        if (application.getStatus() != ApplicationStatus.ADMIN_APPROVED) {
            throw new IllegalStateException("La solicitud no está en estado ADMIN_APPROVED.");
        }

        // ── CONFIRMAR EL PLACEMENT ──
        application.setStatus(ApplicationStatus.CONFIRMED);
        application.setStage(5);
        application.setStatusUpdatedAt(LocalDateTime.now());

        // Decrementar plazas disponibles en la oferta
        Opportunity opportunity = application.getOpportunity();
        opportunity.setSpotsAvailable(Math.max(0, opportunity.getSpotsAvailable() - 1));
        if (opportunity.getSpotsAvailable() == 0) {
            opportunity.setStatus(Opportunity.OpportunityStatus.CLOSED);
        }
        opportunityRepository.save(opportunity);

        Application confirmed = applicationRepository.save(application);

        // ── CANCELAR OTRAS SOLICITUDES DEL ESTUDIANTE ──
        // Regla de negocio: al confirmar prácticas, se cierra la búsqueda activa
        applicationRepository.cancelOtherApplications(
            application.getStudent().getId(), applicationId);

        // Notificar a todas las partes
        emailService.sendPlacementConfirmedEmail(application);

        log.info("PLACEMENT CONFIRMADO: estudiante={} oferta={} aplicacion={}",
            application.getStudent().getId(), opportunity.getId(), applicationId);

        return confirmed;
    }

    /**
     * La Escuela RECHAZA el placement en la Etapa 5.
     * Transición: ADMIN_APPROVED → REJECTED
     */
    public Application schoolRejectPlacement(Long applicationId, Long schoolId, String reason) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + applicationId));

        if (!application.getStudent().getSchool().getId().equals(schoolId)) {
            throw new AccessDeniedException("Esta escuela no puede rechazar esta solicitud.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setStatusUpdatedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        emailService.sendPlacementRejectedEmail(application, reason);
        return saved;
    }

    /**
     * La empresa RECHAZA a un candidato.
     * Válido en etapas 1 (APPLIED) o 2 (INTERESTED).
     */
    public Application rejectCandidate(Long applicationId, Long companyId, String reason) {
        Application application = findAndValidateForCompany(applicationId, companyId);

        if (application.getStatus() != ApplicationStatus.APPLIED &&
            application.getStatus() != ApplicationStatus.INTERESTED) {
            throw new IllegalStateException("No se puede rechazar en este estado.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setStatusUpdatedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        emailService.sendRejectionEmail(application.getStudent(), application.getOpportunity(), reason);
        return saved;
    }

    /**
     * El estudiante RETIRA su propia solicitud.
     * Solo válido si la solicitud está en Etapa 1 o 2.
     */
    public Application withdrawApplication(Long applicationId, Long studentId) {
        Application application = findAndValidateForStudent(applicationId, studentId);

        if (application.getStage() > 2) {
            throw new IllegalStateException(
                "No se puede retirar una solicitud en etapa avanzada (etapa 3 o superior).");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setStatusUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    /**
     * Obtiene las solicitudes de un estudiante paginadas.
     * Incluye el historial completo de estados.
     */
    @Transactional(readOnly = true)
    public Page<Application> getStudentApplications(Long studentId, Pageable pageable) {
        return applicationRepository.findByStudentId(studentId, pageable);
    }

    /**
     * Obtiene los candidatos de una oferta específica, para revisión por la empresa.
     */
    @Transactional(readOnly = true)
    public Page<Application> getOpportunityApplications(Long opportunityId, Pageable pageable) {
        return applicationRepository.findByOpportunityId(opportunityId, pageable);
    }

    // ────────────────────────────────────────────────────
    // Métodos privados de ayuda
    // ────────────────────────────────────────────────────

    /**
     * Busca una solicitud y verifica que pertenece a la empresa indicada.
     * Lanza AccessDeniedException si la oferta no es de esa empresa.
     */
    private Application findAndValidateForCompany(Long applicationId, Long companyId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + applicationId));

        if (!application.getOpportunity().getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("No tienes permiso para actuar sobre esta solicitud.");
        }
        return application;
    }

    /**
     * Busca una solicitud y verifica que pertenece al estudiante indicado.
     */
    private Application findAndValidateForStudent(Long applicationId, Long studentId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + applicationId));

        if (!application.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("No tienes permiso para actuar sobre esta solicitud.");
        }
        return application;
    }

    /**
     * Valida que la transición de estado es válida.
     * Lanza IllegalStateException si el estado actual no es el esperado.
     */
    private void validateTransition(Application application, ApplicationStatus expectedStatus) {
        if (application.getStatus() != expectedStatus) {
            throw new IllegalStateException(String.format(
                "Transición inválida: el estado actual es %s, se esperaba %s.",
                application.getStatus(), expectedStatus));
        }
    }
}
