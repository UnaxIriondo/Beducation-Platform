package com.beducation.controller;

import com.beducation.model.*;
import com.beducation.repository.*;
import com.beducation.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * CONTROLADOR DE DESARROLLO: DebugController
 * -----------------------------------------
 * ATENCIÓN: Este controlador contiene atajos de seguridad y herramientas 
 * de limpieza de base de datos exclusivas para el desarrollo local.
 * 
 * Este componente SOLO se carga si el perfil activo NO es 'prod'.
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
@Profile("!prod")
@Tag(name = "Development Tools", description = "Endpoints de utilidad para agilizar el desarrollo y pruebas locales.")
public class DebugController {


    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final CompanyRepository companyRepository;
    private final OpportunityRepository opportunityRepository;

    private final ApplicationRepository applicationRepository;
    private final StudentService studentService;

    @PostMapping("/reset-database")

    @Transactional
    @Operation(summary = "LIMPIAR TODA LA BD (Solo Dev)", description = "Borra aplicaciones, ofertas, alumnos, empresas y centros de la base de datos.")
    public ResponseEntity<?> resetDatabase() {
        applicationRepository.deleteAll();
        opportunityRepository.deleteAll();
        studentRepository.deleteAll();
        companyRepository.deleteAll();
        schoolRepository.deleteAll();
        // Borrar usuarios que no sean administradores críticos si existen, 
        // o borrar todos para empezar de cero (el login debug los recrea).
        userRepository.deleteAll();
        
        System.out.println("DEBUG: Base de datos reseteada por completo.");
        return ResponseEntity.ok(Map.of("message", "Base de datos limpiada con éxito. Todos los mocks han sido eliminados."));
    }

    @GetMapping("/login")
    @Operation(summary = "Login rápido por email (Sin contraseña)", description = "Busca al usuario por email o crea uno nuevo. Retorna un JWT firmado localmente para bypass de Auth0.")
    public ResponseEntity<?> loginByEmail(@RequestParam String email) {
        final String trimmedEmail = email.trim();
        
        // 1. Intentar buscar usuario existente
        User userToReturn = userRepository.findByEmail(trimmedEmail).orElse(null);

        // 2. Si no existe, crear un mock básico
        if (userToReturn == null) {
            User.Role assignedRole;
            if (trimmedEmail.contains("admin")) assignedRole = User.Role.ADMIN;
            else if (trimmedEmail.contains("company")) assignedRole = User.Role.COMPANY;
            else if (trimmedEmail.contains("student") || trimmedEmail.contains("ikasle")) assignedRole = User.Role.STUDENT;
            else assignedRole = User.Role.SCHOOL;
            
            userToReturn = User.builder()
                .email(trimmedEmail)
                .auth0Id("debug-mock-id-" + trimmedEmail.replace("@", "-at-"))
                .role(assignedRole)
                .status(User.UserStatus.ACTIVE)
                .build();
            userToReturn = userRepository.save(userToReturn);
            System.out.println("DEBUG: Creado usuario básico para: " + trimmedEmail);
        }

        final User finalUser = userToReturn;

        // Asegurar entidades mínimas para evitar errores de dashboard vacío
        if (finalUser.getRole() == User.Role.SCHOOL && schoolRepository.findByUserId(finalUser.getId()).isEmpty()) {
            schoolRepository.save(School.builder()
                .user(finalUser).name("Centro " + finalUser.getId())
                .country("Spain").city("Vitoria").status(School.ApprovalStatus.APPROVED)
                .institutionType(School.InstitutionType.CONCERTADA).build());
        }
        if (finalUser.getRole() == User.Role.COMPANY && companyRepository.findByUserId(finalUser.getId()).isEmpty()) {
            companyRepository.save(Company.builder()
                .user(finalUser).name("Empresa " + finalUser.getId())
                .country("Ireland").city("Dublin").status(School.ApprovalStatus.APPROVED)
                .sector("Services").build());
        }
        if (finalUser.getRole() == User.Role.STUDENT) {
            // Sincronizar usuario con alumno invitado si existe
            Student syncedStudent = studentService.syncStudentWithUser(finalUser);
            
            if (syncedStudent == null) {
                // Si no existía invitación previa, creamos uno de cero como antes
                School debugSchool = schoolRepository.findAll().stream().findFirst().orElseGet(() -> {
                    return schoolRepository.save(School.builder()
                        .name("Escuela de Pruebas (Debug)")
                        .country("Spain").city("Vitoria").status(School.ApprovalStatus.APPROVED)
                        .institutionType(School.InstitutionType.CONCERTADA).build());
                });

                studentRepository.save(Student.builder()
                    .user(finalUser)
                    .school(debugSchool)
                    .firstName(finalUser.getEmail().split("@")[0])
                    .lastName("Dev")
                    .invitationEmail(finalUser.getEmail())
                    .profileComplete(false)
                    .build());
            }
        }

        Map<String, String> response = new HashMap<>();
        String mockToken = "mock-jwt-token~" + finalUser.getAuth0Id() + "~" + finalUser.getRole().name();
        response.put("token", mockToken);
        response.put("role", finalUser.getRole().name());
        response.put("email", finalUser.getEmail());
        return ResponseEntity.ok(response);
    }

}
