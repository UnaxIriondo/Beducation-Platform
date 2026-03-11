package com.beducation.repository;

import com.beducation.model.KeywordCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio CRUD para categorías de Keywords. */
@Repository
public interface KeywordCategoryRepository extends JpaRepository<KeywordCategory, Long> {
    boolean existsByName(String name);
}
