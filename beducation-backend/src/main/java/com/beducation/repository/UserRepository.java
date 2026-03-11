package com.beducation.repository;

import com.beducation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * Repositorio: UserRepository
 * ============================================================
 * Acceso a la tabla "users" en la base de datos.
 * Spring Data JPA genera automáticamente las implementaciones
 * de los métodos declarados aquí.
 * ============================================================
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su ID de Auth0 ("sub" del JWT).
     * Usado al validar el token JWT para identificar al usuario.
     * @param auth0Id el identificador único de Auth0
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<User> findByAuth0Id(String auth0Id);

    /**
     * Busca un usuario por su dirección de email.
     * @param email email del usuario
     * @return Optional con el usuario encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con ese email.
     * @param email email a verificar
     * @return true si ya existe
     */
    boolean existsByEmail(String email);
}
