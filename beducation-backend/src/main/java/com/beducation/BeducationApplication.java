package com.beducation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================
 * CLASE PRINCIPAL: BeDucationApplication
 * ============================================================
 * Inicializador de Spring Boot. 
 * Contiene la habilitación del motor de Tasks (@EnableScheduling)
 * que usaremos para desechar o mover a EXPIRED las solicitudes
 * que lleven 30 días inactivas sin update de estado.
 * ============================================================
 */
@SpringBootApplication
@EnableScheduling
public class BeducationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeducationApplication.class, args);
    }
}
