package com.beducation.dto;

import com.beducation.model.School;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolProfileDto {
    private Long id;

    @NotBlank(message = "El nombre de la institución es obligatorio")
    private String name;
    
    private School.InstitutionType institutionType;
    
    private String description;
    
    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String contactEmail;
    
    private String phone;
    private String address;
    private String website;
    private String contactPerson;

    /**
     * Convierte una entidad School a un DTO (para el GET /me)
     */
    public static SchoolProfileDto fromEntity(School school) {
        return SchoolProfileDto.builder()
            .id(school.getId())
            .name(school.getName())
            .institutionType(school.getInstitutionType())
            .description(school.getDescription())
            .contactEmail(school.getUser().getEmail()) // Asumimos que el email principal es el de contacto
            .phone(school.getPhone())
            .address(school.getAddress())
            .website(school.getWebsite())
            .contactPerson(school.getContactPerson())
            .build();
    }
}
