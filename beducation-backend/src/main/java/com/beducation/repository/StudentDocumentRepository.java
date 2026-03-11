package com.beducation.repository;

import com.beducation.model.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para documentos de estudiantes.
 * Permite obtener el documento más reciente de cada tipo.
 */
@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {

    /** Lista todos los documentos de un estudiante. */
    List<StudentDocument> findByStudentId(Long studentId);

    /**
     * Obtiene el documento más reciente de un tipo específico.
     * Ordenado por fecha de subida descendente → primero el más nuevo.
     */
    Optional<StudentDocument> findTopByStudentIdAndDocumentTypeOrderByUploadedAtDesc(
        Long studentId, StudentDocument.DocumentType documentType);

    /**
     * Verifica si un estudiante tiene al menos un documento de un tipo concreto.
     * Usado para validar si el perfil es completo (CV obligatorio).
     */
    boolean existsByStudentIdAndDocumentType(Long studentId, StudentDocument.DocumentType documentType);
}
