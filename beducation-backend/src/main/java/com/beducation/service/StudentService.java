package com.beducation.service;

import com.beducation.dto.StudentProfileDto;
import com.beducation.model.*;
import com.beducation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * Servicio: StudentService
 * ============================================================
 * Maneja el onboarding inicial de los estudiantes invitados, la 
 * gestión de su perfil, subir documentos del CV y calcula
 * la variable ProfileComplete, requerida para poder aplicar.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final KeywordRepository keywordRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final S3StorageService s3StorageService;

    // ──────────────────────────────────────────────
    // GESTIÓN DEL PERFIL DE ESTUDIANTE
    // ──────────────────────────────────────────────

    /**
     * Completa o actualiza el perfil en el Onboarding o desde el portal 
     * de Estudiante. Posteriormente verifica si el perfil está completo 
     * según las reglas de negocio.
     */
    public Student updateProfile(Long studentId, StudentProfileDto dto) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID " + studentId));

        if (dto.getFirstName() != null) student.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) student.setLastName(dto.getLastName());
        if (dto.getPhone() != null) student.setPhone(dto.getPhone());
        if (dto.getMotivation() != null) student.setMotivation(dto.getMotivation());

        if (dto.getEducationTypeId() != null) {
            EducationType type = educationTypeRepository.findById(dto.getEducationTypeId())
                .orElseThrow(() -> new RuntimeException("Tipo de formación inválido."));
            student.setEducationType(type);
        }

        if (dto.getKeywordIds() != null) {
            List<Keyword> keywords = keywordRepository.findAllById(dto.getKeywordIds());
            student.setKeywords(keywords);
        }

        if (dto.getCountryPreferences() != null && dto.getCountryPreferences().size() <= 5) {
            student.setCountryPreferences(dto.getCountryPreferences());
        }

        student.setOnboardingComplete(true);
        student.setUpdatedAt(LocalDateTime.now());
        
        // Se ejecuta la comprobación de completado siempre que hay edición de perfil.
        checkProfileComplete(student);
        
        return studentRepository.save(student);
    }
    
    public Student getStudentProfile(Long studentId) {
        return studentRepository.findById(studentId)
        	.orElseThrow(() -> new RuntimeException("Estudiante no encontrado."));
    }

    // ──────────────────────────────────────────────
    // SUBIDA DE DOCUMENTOS (CV, CARTA, FOTO) A AWS S3
    // ──────────────────────────────────────────────

    /**
     * Sube y asocia un documento al perfil del estudiante. El documento
     * se persiste físicamente en S3 y sus metadatos en la base de datos MySQL.
     * Retorna el objeto StudentDocument creado.
     */
    public StudentDocument uploadDocument(Long studentId, StudentDocument.DocumentType type, MultipartFile file) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        String folder = "students/" + student.getAuth0Id() + "/docs";
        String s3Key = s3StorageService.uploadFile(file, folder);

        StudentDocument document = StudentDocument.builder()
            .student(student)
            .documentType(type)
            .s3Key(s3Key)
            .originalFilename(file.getOriginalFilename())
            .fileSizeBytes(file.getSize())
            .contentType(file.getContentType())
            .build();

        StudentDocument savedDoc = studentDocumentRepository.save(document);
        student.getDocuments().add(savedDoc);
        
        // Volvemos a chequear si se completa el perfil, 
        // porque el CV es el requisito bloqueante más importante.
        checkProfileComplete(student);
        studentRepository.save(student);

        log.info("Documento de tipo {} subido para el estudiante M/ID: {}", type, student.getId());
        return savedDoc;
    }

    // ──────────────────────────────────────────────
    // LÓGICA DE NEGOCIO: VALIDACIÓN DE PERFIL COMPLETO
    // ──────────────────────────────────────────────

    /**
     * Verifica los criterios del organigrama "flujo de trabajo estudiante"
     * para rellenar la variable booleana limitante 'profileComplete'.
     */
    private void checkProfileComplete(Student student) {
        boolean hasMotivation = student.getMotivation() != null && student.getMotivation().length() >= 100;
        boolean hasEducation = student.getEducationType() != null;
        boolean hasKeywords = student.getKeywords() != null && !student.getKeywords().isEmpty();
        boolean hasCountries = student.getCountryPreferences() != null && !student.getCountryPreferences().isEmpty();
        boolean hasCv = studentDocumentRepository.existsByStudentIdAndDocumentType(student.getId(), StudentDocument.DocumentType.CV);

        student.setProfileComplete(hasMotivation && hasEducation && hasKeywords && hasCountries && hasCv);
    }
}
