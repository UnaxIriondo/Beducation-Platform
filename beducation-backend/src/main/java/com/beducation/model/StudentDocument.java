package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Entidad: StudentDocument
 * ============================================================
 * Almacena los metadatos de un documento subido por un estudiante.
 *
 * Los archivos físicos se almacenan en AWS S3.
 * Esta entidad solo guarda la referencia (clave S3) y metadatos.
 *
 * Tipos de documentos:
 *   CV           → Obligatorio para perfil completo
 *   COVER_LETTER → Opcional (carta de presentación)
 *   PHOTO        → Opcional (foto del estudiante)
 *
 * Un estudiante puede subir múltiples versiones del mismo tipo,
 * pero solo se considera vigente el último subido.
 * ============================================================
 */
@Entity
@Table(name = "student_documents", indexes = {
    @Index(name = "idx_doc_student", columnList = "student_id"),
    @Index(name = "idx_doc_type", columnList = "document_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StudentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Estudiante propietario del documento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Tipo de documento: CV, COVER_LETTER o PHOTO.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    /**
     * Clave del objeto en AWS S3.
     * Formato: "students/{studentId}/cv/{uuid}.pdf"
     * Para generar la URL pública o pre-signed: s3Client.getUrl(bucket, s3Key)
     */
    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    /** Nombre original del archivo tal como lo subió el estudiante. */
    @Column(name = "original_filename")
    private String originalFilename;

    /** Tamaño del archivo en bytes. */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    /** Tipo MIME del archivo (application/pdf, image/jpeg, etc.). */
    @Column(name = "content_type")
    private String contentType;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Tipos de documentos permitidos para estudiantes.
     */
    public enum DocumentType {
        CV,            // Curriculum Vitae — obligatorio
        COVER_LETTER,  // Carta de presentación — opcional
        PHOTO          // Foto del estudiante — opcional
    }
}
