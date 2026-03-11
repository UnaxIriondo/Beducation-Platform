package com.beducation.dto;

import lombok.Data;
import java.util.List;

/**
 * ============================================================
 * DTO: StudentProfileDto
 * ============================================================
 * Transferencia de datos desde el frontend para actualizar 
 * el perfil del estudiante en el flujo de Onboarding.
 * ============================================================
 */
@Data
public class StudentProfileDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String motivation;
    private Long educationTypeId;
    private List<Long> keywordIds;
    private List<String> countryPreferences;
}
