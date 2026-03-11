package com.beducation.repository;

import com.beducation.model.Keyword;
import com.beducation.model.KeywordCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio CRUD para Keywords. Gestión por el Admin. */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByCategoryOrderByName(KeywordCategory category);
    List<Keyword> findAllByOrderByCategoryNameAscNameAsc();
}
