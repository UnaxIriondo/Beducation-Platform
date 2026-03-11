package com.beducation.repository;

import com.beducation.model.EducationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD para tipos de formación académica. */
@Repository
public interface EducationTypeRepository extends JpaRepository<EducationType, Long> {
    boolean existsByCode(String code);
}
