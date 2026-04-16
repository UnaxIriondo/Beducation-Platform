package com.beducation.service;

import com.beducation.model.GalleryAccessRequest;
import com.beducation.model.GalleryAccessRequest.AccessStatus;
import com.beducation.model.Photo;
import com.beducation.model.User;
import com.beducation.repository.GalleryAccessRequestRepository;
import com.beducation.repository.PhotoRepository;
import com.beducation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * Servicio: PhotoService
 * ============================================================
 * Gestiona la subida de fotos por el Admin y el control de 
 * acceso a la galería para alumnos y empresas.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final GalleryAccessRequestRepository accessRequestRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final EmailService emailService;

    // ──────────────────────────────────────────────
    // GESTIÓN DE FOTOS (ADMIN)
    // ──────────────────────────────────────────────

    /**
     * Sube múltiples fotos a S3 y guarda sus referencias en la DB.
     * Solo debe ser llamado por el Administrador.
     */
    public List<Photo> uploadPhotos(MultipartFile[] files, String description) {
        java.util.List<Photo> uploadedPhotos = new java.util.ArrayList<>();
        
        for (MultipartFile file : files) {
            String s3Key = s3StorageService.uploadFile(file, "gallery");
            
            Photo photo = Photo.builder()
                .s3Key(s3Key)
                .description(description)
                .build();
            
            uploadedPhotos.add(photoRepository.save(photo));
            log.info("Foto subida por Admin en lote: {}", s3Key);
        }
        
        return uploadedPhotos;
    }

    /**
     * Elimina una foto de la galería y de S3.
     */
    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
            .orElseThrow(() -> new RuntimeException("Foto no encontrada"));
            
        s3StorageService.deleteFile(photo.getS3Key());
        photoRepository.delete(photo);
        log.info("Foto eliminada de la galería: {}", photoId);
    }

    /**
     * Lista todas las fotos disponibles (para uso del Admin o usuarios aprobados).
     */
    @Transactional(readOnly = true)
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    // ──────────────────────────────────────────────
    // CONTROL DE ACCESO (STUDENTS / COMPANIES)
    // ──────────────────────────────────────────────

    /**
     * Verifica si un usuario tiene acceso a la galería.
     * Los Admins siempre tienen acceso.
     */
    @Transactional(readOnly = true)
    public boolean hasAccess(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        if (user.getRole() == User.Role.ADMIN) return true;
        
        return accessRequestRepository.existsByUserIdAndStatus(userId, AccessStatus.APPROVED);
    }

    /**
     * Solicita acceso a la galería.
     */
    public GalleryAccessRequest requestAccess(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Evitar duplicados si ya hay una pendiente o aprobada
        if (accessRequestRepository.existsByUserIdAndStatus(userId, AccessStatus.PENDING) ||
            accessRequestRepository.existsByUserIdAndStatus(userId, AccessStatus.APPROVED)) {
            throw new IllegalStateException("Ya tienes una solicitud de acceso enviada o aprobada.");
        }

        GalleryAccessRequest request = GalleryAccessRequest.builder()
            .user(user)
            .status(AccessStatus.PENDING)
            .build();
            
        log.info("Solicitud de acceso a galería enviada por usuario: {}", user.getEmail());
        return accessRequestRepository.save(request);
    }

    /**
     * Obtiene la solicitud de acceso actual del usuario.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<GalleryAccessRequest> getMyRequest(Long userId) {
        return accessRequestRepository.findFirstByUserIdOrderByRequestedAtDesc(userId);
    }

    // ──────────────────────────────────────────────
    // GESTIÓN DE SOLICITUDES (ADMIN)
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<GalleryAccessRequest> getPendingRequests(Pageable pageable) {
        return accessRequestRepository.findByStatus(AccessStatus.PENDING, pageable);
    }

    public GalleryAccessRequest processRequest(Long requestId, boolean approve, String reason) {
        GalleryAccessRequest request = accessRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        request.setStatus(approve ? AccessStatus.APPROVED : AccessStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRejectionReason(reason);
        
        GalleryAccessRequest saved = accessRequestRepository.save(request);
        
        // Notificar al usuario por email
        if (approve) {
            emailService.sendGalleryAccessApprovedEmail(request.getUser());
        } else {
            emailService.sendGalleryAccessRejectedEmail(request.getUser(), reason);
        }
        
        log.info("Solicitud de acceso {} procesada: {}", requestId, request.getStatus());
        return saved;
    }
}
