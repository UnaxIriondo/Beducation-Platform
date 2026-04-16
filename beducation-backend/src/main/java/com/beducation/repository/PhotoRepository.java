package com.beducation.repository;

import com.beducation.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ============================================================
 * Repositorio: PhotoRepository
 * ============================================================
 * Gestión de persistencia para las fotografías de la galería.
 * ============================================================
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
