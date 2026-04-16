package com.beducation.service;

import com.beducation.dto.StudentProfileDto;
import com.beducation.model.*;
import com.beducation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        // 1. Buscar si ya existe un documento de este tipo para borrarlo (Limpieza)
        studentDocumentRepository.findByStudentId(studentId).stream()
            .filter(doc -> doc.getDocumentType() == type)
            .forEach(oldDoc -> {
                log.info("Eliminando documento antiguo de tipo {} para estudiante {}: {}", type, studentId, oldDoc.getS3Key());
                s3StorageService.deleteFile(oldDoc.getS3Key());
                studentDocumentRepository.delete(oldDoc);
                // Lo quitamos de la lista local para que Hibernate no intente re-guardarlo
                student.getDocuments().remove(oldDoc);
            });

        // 2. Subir el nuevo
        String folder = "students/" + student.getId() + "/docs";
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

    /**
     * Resultado de una invitación.
     */
    public record InvitationResult(Student student) {}

    /**
     * Lógica atómica de invitación (un solo estudiante).
     * Se usa REQUIRES_NEW para que si falla un envío de email o un registro,
     * no se rompa la transacción global de una importación masiva.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InvitationResult inviteStudent(School school, String firstName, String lastName, String email, String educationCode) {
        if (studentRepository.existsByInvitationEmail(email)) {
            log.info("El estudiante con email {} ya está invitado. Saltando.", email);
            return null; 
        }

        Student.StudentBuilder studentBuilder = Student.builder()
            .school(school)
            .firstName(firstName)
            .lastName(lastName)
            .invitationEmail(email)
            .invitedAt(LocalDateTime.now());

        if (educationCode != null && !educationCode.isBlank()) {
            educationTypeRepository.findByCode(educationCode)
                .ifPresent(studentBuilder::educationType);
        }

        Student saved = studentRepository.save(studentBuilder.build());
        log.info("Estudiante {} guardado correctamente (Transacción Independiente).", email);
        return new InvitationResult(saved);
    }

    @Transactional(readOnly = true)
    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("No se encontró perfil de estudiante para el usuario ID: " + userId));
    }

    /**
     * Sincroniza un usuario recién autenticado con un registro de estudiante
     * invitado previamente por email.
     */
    public Student syncStudentWithUser(User user) {
        // 1. Verificar si ya está vinculado
        Optional<Student> existingLinked = studentRepository.findByUserId(user.getId());
        if (existingLinked.isPresent()) return existingLinked.get();

        // 2. Buscar por email de invitación (Trim para seguridad con import CSV)
        String userEmail = user.getEmail() != null ? user.getEmail().trim() : "";
        
        return studentRepository.findByInvitationEmail(userEmail)
            .map(student -> {
                if (student.getUser() == null) {
                    student.setUser(user);
                    student.setRegisteredAt(LocalDateTime.now());
                    log.info("Sincronizado alumno {} con User ID {}", student.getInvitationEmail(), user.getId());
                    return studentRepository.save(student);
                }
                return student;
            })
            .orElse(null);
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
