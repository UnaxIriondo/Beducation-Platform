package com.beducation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ============================================================
 * DTO: CompanyRegistrationDto
 * ============================================================
 */
@Data
public class CompanyRegistrationDto {
    @NotBlank
    private String auth0Id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String name;
    @NotBlank
    private String country;
    
    private String city;
    private String website;
    private String description;
    private String companySize;
    private String sector;
}
