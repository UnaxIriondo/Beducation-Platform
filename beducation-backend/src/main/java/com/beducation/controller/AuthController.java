package com.beducation.controller;

import com.beducation.model.Student;
import com.beducation.model.User;
import com.beducation.repository.StudentRepository;
import com.beducation.repository.UserRepository;
import com.beducation.service.JwtService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ============================================================
 * Controlador: AuthController
 * ============================================================
 * Maneja el inicio de sesión local (híbrido) para estudiantes
 * que han sido invitados con una contraseña temporal.
 * ============================================================
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        log.info("Intento de login local para: [{}]", email);

        // 1. Buscar si ya existe el usuario consolidado
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("DEBUG: Usuario encontrado en tabla 'users'. ID: {}, Role: {}", user.getId(), user.getRole());
            log.info("DEBUG: ¿Tiene password en DB?: {}", (user.getPassword() != null));
            
            if (user.getPassword() != null) {
                boolean matches = passwordEncoder.matches(password, user.getPassword());
                log.info("DEBUG: Resultado de la comparacion PasswordEncoder.matches: {}", matches);
                
                if (matches) {
                    return generateLoginResponse(user);
                }
            } else {
                log.warn("DEBUG: El usuario existe pero el campo 'password' está NULL en la tabla 'users'.");
            }
            return ResponseEntity.status(401).body("Credenciales inválidas.");
        }

        // 2. Si no existe el usuario, buscar si es un estudiante invitado pendiente de consolidar
        log.info("DEBUG: Usuario no hallado en tabla 'users'. Buscando en tabla 'students'...");
        Optional<Student> studentOpt = studentRepository.findByInvitationEmail(email);
        
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            log.info("DEBUG: Invitación encontrada en tabla 'students'. ID: {}", student.getId());
            if (student.getTempPassword() != null && passwordEncoder.matches(password, student.getTempPassword())) {
                // PRIMER LOGIN: Consolidar usuario
                log.info("Consolidando usuario para el estudiante: {}", email);
                
                User newUser = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password)) // Guardamos la contraseña definitiva (puede ser la misma)
                        .role(User.Role.STUDENT)
                        .status(User.UserStatus.ACTIVE)
                        .build();
                
                User savedUser = userRepository.save(newUser);
                
                // Vincular estudiante al nuevo usuario
                student.setUser(savedUser);
                student.setRegisteredAt(LocalDateTime.now());
                student.setTempPassword(null); // Borramos la temporal
                studentRepository.save(student);
                
                return generateLoginResponse(savedUser);
            }
        }

        return ResponseEntity.status(401).body("No se encontró ninguna invitación o cuenta para este email.");
    }

    private ResponseEntity<LoginResponse> generateLoginResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @Builder
    public static class LoginResponse {
        private String token;
        private String email;
        private String role;
    }
}
