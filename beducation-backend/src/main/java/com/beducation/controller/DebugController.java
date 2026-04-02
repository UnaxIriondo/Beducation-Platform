package com.beducation.controller;

import com.beducation.model.School;
import com.beducation.model.User;
import com.beducation.repository.UserRepository;
import com.beducation.repository.StudentRepository;
import com.beducation.repository.SchoolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * Controlador de Desarrollo: DebugController
 * ============================================================
 * Este controlador solo sirve para facilitar las pruebas al equipo
 * frontend, permitiendo "simular" un login de cualquier usuario de
 * la base de datos sin necesitar la contraseña real de Auth0.
 *
 * PROHIBIDO EN PRODUCCIÓN.
 * ============================================================
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
@Tag(name = "Development / Debug Tools", description = "Backdoor para login rápido de desarrollo.")
public class DebugController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/fix-password")
    @Operation(summary = "REPARAR PASSWORDS (Solo Dev)", description = "Sobreescribe el password de Unax con '123456' para pruebas.")
    public String fixPassword() {
        String email = "unax.iriondo.51345@ikasle.egibide.org";
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(user);
            return "OK: Password de " + email + " reseteada a '123456'";
        }
        return "ERROR: No se hallo al usuario " + email;
    }

    @GetMapping("/login")
    @Operation(summary = "Login rápido por email (Sin contraseña)", description = "Busca al usuario por email. Si no existe pero es un alumno invitado, crea el usuario mock al vuelo.")
    public ResponseEntity<?> loginByEmail(@RequestParam String email) {
        String trimmedEmail = email.trim();
        // 1. Intentar buscar usuario existente
        User user = userRepository.findByEmail(trimmedEmail).orElse(null);

        // 2. Si no existe, ver si es un estudiante invitado por una escuela (vía CSV)
        if (user == null) {
            var studentOpt = studentRepository.findByInvitationEmail(trimmedEmail);
            if (studentOpt.isPresent()) {
                // Crear usuario al vuelo para este estudiante invitado
                user = User.builder()
                    .email(trimmedEmail)
                    .auth0Id("debug-mock-id-" + trimmedEmail.replace("@", "-at-"))
                    .role(User.Role.STUDENT)
                    .status(User.UserStatus.ACTIVE)
                    .build();
                user = userRepository.save(user);
                System.out.println("DEBUG: Creado usuario mock para estudiante invitado: " + trimmedEmail);
            }
            // If still no user, create a generic mock user (e.g., for school testing)
            if (user == null) {
                user = User.builder()
                    .email(trimmedEmail)
                    .auth0Id("debug-mock-id-" + trimmedEmail.replace("@", "-at-"))
                    .role(User.Role.SCHOOL)
                    .status(User.UserStatus.ACTIVE)
                    .build();
                user = userRepository.save(user);
                System.out.println("DEBUG: Creado usuario mock genérico para email: " + trimmedEmail);
            }
        }

        // Ensure mock schools have a School entity
        if (user != null && user.getRole() == User.Role.SCHOOL) {
            boolean hasSchool = schoolRepository.findByUserId(user.getId()).isPresent();
            if (!hasSchool) {
                School mockSchool = School.builder()
                    .user(user)
                    .name("Mock Debug School " + user.getId())
                    .country("Spain")
                    .city("Debug City")
                    .address("Debug Address 123")
                    .status(School.ApprovalStatus.APPROVED)
                    .institutionType(School.InstitutionType.CONCERTADA)
                    .build();
                schoolRepository.save(mockSchool);
                System.out.println("DEBUG: Creado School mock para usuario: " + trimmedEmail);
            }
        }

        if (user != null) {
            Map<String, String> response = new HashMap<>();
            // Formato: mock-jwt-token~AUTH0_ID~ROLE
            String mockToken = "mock-jwt-token~" + user.getAuth0Id() + "~" + user.getRole().name();
            response.put("token", mockToken);
            response.put("role", user.getRole().name());
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }
}
