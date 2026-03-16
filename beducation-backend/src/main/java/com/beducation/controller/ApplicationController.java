package com.beducation.controller;

import com.beducation.model.Application;
import com.beducation.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Controlador: ApplicationController
 * ============================================================
 * Rutas de control del motor de estados de las Aplicaciones o
 * Solicitudes de Colocación (Placements de prácticas) — Flujo de 5 pasos.
 * ============================================================
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Application Workflow Engine", description = "Endpoints de control que mueven el placement las 5 etapas entre Empleador y Candidato.")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private final ApplicationService applicationService;

    // ── ETAPA 1 (ESTUDIANTE APLICA O DA MARCHA ATRAS) ── //

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "(Stage 1) - Estudiante completa el profile y opta a la oferta.")
    public ResponseEntity<Application> applyOpportunity(
        @RequestParam Long studentId, 
        @RequestParam Long opportunityId) {
        
        Application app = applicationService.apply(studentId, opportunityId);
        return new ResponseEntity<>(app, HttpStatus.CREATED);
    }

    @DeleteMapping("/{appId}/withdraw")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "(Stage 1 y 2) - Estudiante aborta proactivamente su aplicación y la da por WITHDRAWN.")
    public ResponseEntity<Application> withdraw(
        @PathVariable Long appId,
        @RequestParam Long studentId) {
        return ResponseEntity.ok(applicationService.withdrawApplication(appId, studentId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "Trackeo en Dashboard de estado procesado que ve cada Estudiante de todas las aplicadas.")
    public ResponseEntity<Page<Application>> studentTrack(
        @PathVariable Long studentId, Pageable pageable) {
        return ResponseEntity.ok(applicationService.getStudentApplications(studentId, pageable));
    }

    // ── ETAPA 2 y ETAPA 3 (COMPAÑÍA EVALÚA/OFERTA O RECHAZA) ── //

    @GetMapping("/opportunity/{opportunityId}")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Dashboard Listado de Postulantes de la Oferta (Muestra si están completos, matching y status).")
    public ResponseEntity<Page<Application>> fetchCandidates(
        @PathVariable Long opportunityId, Pageable pageable) {
        return ResponseEntity.ok(applicationService.getOpportunityApplications(opportunityId, pageable));
    }

    @PatchMapping("/{appId}/express-interest")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "(Stage 2) - Empresa declara Interested en el estudiante evaluado.")
    public ResponseEntity<Application> interested(
        @PathVariable Long appId,
        @RequestParam Long companyId) {
        return ResponseEntity.ok(applicationService.expressInterest(appId, companyId));
    }

    @PostMapping("/{appId}/interview")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "(Post-Stage 2) - Empresa setea cita/entrevista")
    public ResponseEntity<Application> scheduleInterview(
        @PathVariable Long appId,
        @RequestParam Long companyId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
        @RequestParam String videoCallLink,
        @RequestParam(required = false) String notes) {
        
        Application app = applicationService.scheduleInterview(appId, companyId, scheduledAt, videoCallLink, notes);
        return ResponseEntity.ok(app);
    }

    @PatchMapping("/{appId}/offer")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "(Stage 3) - Empresa publica la oferta de vacante oficial al estudiante.")
    public ResponseEntity<Application> makeOffer(
        @PathVariable Long appId,
        @RequestParam Long companyId) {
        return ResponseEntity.ok(applicationService.makeOffer(appId, companyId));
    }

    @PatchMapping("/{appId}/reject")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Empresa da Feedback negativo en Etapas 1-2 sobre el CV del estudiante o entrevista y rechaza la candidadura.")
    public ResponseEntity<Application> rejectCandidate(
        @PathVariable Long appId,
        @RequestParam Long companyId,
        @RequestParam String reason) {
        return ResponseEntity.ok(applicationService.rejectCandidate(appId, companyId, reason));
    }

    // ── ETAPA 3b/c (ESTUDIANTE ACEPTA/RECHAZA OFFER OBTENIDA) ── //

    @PatchMapping("/{appId}/accept")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "(Final Stage 3->4) Estudiante se muestra a favor de la Start Date, y avanza a revision.")
    public ResponseEntity<Application> acceptOffer(
        @PathVariable Long appId,
        @RequestParam Long studentId) {
        return ResponseEntity.ok(applicationService.acceptOffer(appId, studentId));
    }

    @PatchMapping("/{appId}/reject-offer")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "Estudiante rechaza la propuesta elaborada (Offer decline)")
    public ResponseEntity<Application> studentDecliningOffer(
        @PathVariable Long appId,
        @RequestParam Long studentId) {
        return ResponseEntity.ok(applicationService.rejectOffer(appId, studentId));
    }

    // ── ETAPA 5 (ESCUELA VALIDA INTERNAMENTE LA POSICIÓN LOGRADA POR EL ESTUDIANTE) ── //
    
    @PatchMapping("/{appId}/school-approve")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "(Stage 5: Final) Convenio verificado. Resto de Requests en curso del Estudiante se Cancelan y Oferta agota 1 Plazar. CONFIRMED")
    public ResponseEntity<Application> confirmPlacementBySchool(
        @PathVariable Long appId,
        @RequestParam Long schoolId) {
        return ResponseEntity.ok(applicationService.schoolAprovePlacement(appId, schoolId));
    }
    
    @PatchMapping("/{appId}/school-reject")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "La escuela opina que la empresa que ha superado el funnel y Admin no es una práctica legal homologada o convenio roto.")
    public ResponseEntity<Application> rejectPlacementBySchool(
        @PathVariable Long appId,
        @RequestParam Long schoolId,
        @RequestParam String reason) {
        return ResponseEntity.ok(applicationService.schoolRejectPlacement(appId, schoolId, reason));
    }
}
