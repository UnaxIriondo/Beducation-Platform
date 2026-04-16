package com.beducation.controller;

import com.beducation.model.GalleryAccessRequest;
import com.beducation.security.SecurityUtils;
import com.beducation.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * Controlador: GalleryAccessController
 * ============================================================
 * Gestión de permisos de visualización para la galería.
 * ============================================================
 */
@RestController
@RequestMapping("/gallery/access")
@RequiredArgsConstructor
@Tag(name = "Gallery Access")
@SecurityRequirement(name = "bearer-key")
public class GalleryAccessController {

    private final PhotoService photoService;
    private final SecurityUtils securityUtils;

    @PostMapping("/request")
    @PreAuthorize("hasAnyAuthority('SCOPE_STUDENT', 'SCOPE_COMPANY', 'SCOPE_SCHOOL')")
    @Operation(summary = "Solicitar acceso a la galería de fotos")
    public ResponseEntity<GalleryAccessRequest> requestAccess() {
        Long userId = securityUtils.getCurrentUser().getId();
        return ResponseEntity.ok(photoService.requestAccess(userId));
    }

    @GetMapping("/my-request")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver el estado de mi propia solicitud de acceso")
    public ResponseEntity<GalleryAccessRequest> getMyRequest() {
        Long userId = securityUtils.getCurrentUser().getId();
        return photoService.getMyRequest(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Listar solicitudes pendientes de acceso (Solo Admin)")
    public ResponseEntity<Page<GalleryAccessRequest>> getPendingRequests(Pageable pageable) {
        return ResponseEntity.ok(photoService.getPendingRequests(pageable));
    }

    @PatchMapping("/admin/process/{requestId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Aprobar o rechazar solicitud de acceso (Solo Admin)")
    public ResponseEntity<GalleryAccessRequest> processRequest(
        @PathVariable Long requestId,
        @RequestParam boolean approve,
        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(photoService.processRequest(requestId, approve, reason));
    }
}
