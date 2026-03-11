package com.beducation.dto;

import com.beducation.model.Opportunity.OpportunityStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * ============================================================
 * DTO: OpportunityDto
 * ============================================================
 * Manejo de creación y edición (CRUD) de ofertas de prácticas.
 * ============================================================
 */
@Data
public class OpportunityDto {
    private String title;
    private String description;
    private String country;
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer spots;
    private Long educationTypeId;
    private List<Long> keywordIds;
    private OpportunityStatus status;
}
