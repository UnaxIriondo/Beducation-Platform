package com.beducation.controller;

import com.beducation.model.Photo;
import com.beducation.security.SecurityUtils;
import com.beducation.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ============================================================
 * Controlador: PhotoController
 * ============================================================
 * Gestión de las fotografías de la galería pública.
 * ============================================================
 */
@RestController
@RequestMapping("/gallery/photos")
@RequiredArgsConstructor
@Tag(name = "Gallery Photos")
@SecurityRequirement(name = "bearer-key")
public class PhotoController {

    private final PhotoService photoService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Subir nuevas fotos a la galería en lote (Solo Admin)")
    public ResponseEntity<List<Photo>> uploadPhotos(
        @RequestParam("files") MultipartFile[] files,
        @RequestParam(value = "description", required = false) String description) {
        return ResponseEntity.ok(photoService.uploadPhotos(files, description));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener todas las fotos si se tiene permiso de acceso")
    public ResponseEntity<List<Photo>> getPhotos() {
        Long userId = securityUtils.getCurrentUser().getId();
        
        if (!photoService.hasAccess(userId)) {
            throw new AccessDeniedException("No tienes permiso de acceso a la galería. Solicita acceso en el panel.");
        }
        
        return ResponseEntity.ok(photoService.getAllPhotos());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Eliminar una foto (Solo Admin)")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-access")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verifica si el usuario logueado tiene permiso de acceso")
    public ResponseEntity<java.util.Map<String, Boolean>> checkAccess() {
        Long userId = securityUtils.getCurrentUser().getId();
        boolean hasAccess = photoService.hasAccess(userId);
        return ResponseEntity.ok(java.util.Map.of("hasAccess", hasAccess));
    }
}
