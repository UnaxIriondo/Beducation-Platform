package com.beducation.service;

import com.beducation.model.User;
import com.beducation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================
 * Servicio: UserService
 * ============================================================
 * Maneja la gestión principal de usuarios y la seguridad
 * de estado (suspensión, etc.).
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Crea un nuevo usuario en la BD asociado a Auth0.
     */
    public User createUser(String auth0Id, String email, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Ya existe un usuario con email: " + email);
        }

        User user = User.builder()
            .auth0Id(auth0Id)
            .email(email)
            .role(role)
            .status(User.UserStatus.ACTIVE)
            .build();

        log.info("Creando nuevo usuario: {} con rol {}", email, role);
        return userRepository.save(user);
    }

    /**
     * Suspende o reactiva un usuario (Para el Panel de Admin)
     */
    public User changeUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setStatus(status);
        log.info("Usuario {} ({}) cambiado a estado {}", user.getId(), user.getEmail(), status);
        return userRepository.save(user);
    }
}
