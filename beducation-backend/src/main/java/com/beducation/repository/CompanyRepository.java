package com.beducation.repository;

import com.beducation.model.Company;
import com.beducation.model.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ============================================================
 * Repositorio: CompanyRepository
 * ============================================================
 * Acceso a datos de empresas con métodos de búsqueda por
 * estado de aprobación para el panel de administración.
 * ============================================================
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUserId(Long userId);

    Page<Company> findByStatus(School.ApprovalStatus status, Pageable pageable);

    long countByStatus(School.ApprovalStatus status);
}
