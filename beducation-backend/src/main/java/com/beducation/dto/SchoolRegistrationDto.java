package com.beducation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ============================================================
 * DTO: SchoolRegistrationDto
 * ============================================================
 * Datos enviados desde el formulario público de registro
 * de escuela en Vue.js. Se usarán para crear un PENDING School.
 * ============================================================
 */
@Data
public class SchoolRegistrationDto {
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
    private String address;
    private String website;
    
    @NotBlank
    private String contactPerson;
    
    private String phone;
}
