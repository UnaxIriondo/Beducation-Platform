package com.beducation.service;

import com.beducation.model.Application;
import com.beducation.model.Application.ApplicationStatus;
import com.beducation.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * Tarea Programada: ExpirationScheduler
 * ============================================================
 * Regla de abandono: La plataforma BeDucation limpia aquellas
 * aplicaciones/procesos que se han quedado bloqueados o 
 * ignorados (30 días especificados de contrato)
 * Ejecuta todos los días un scan completo a media noche.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpirationScheduler {

    private final ApplicationRepository applicationRepository;

    /**
     * Tarea cron que se ejecuta todos los días a las 01:00 AM (servidor)
     * "0 0 1 * * ?" = segundos, minutos, horas, dia mes, mes, dia semana 
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void expireOldApplications() {
        log.info("Iniciando escaneo de Solicitudes expiradas (más de 30 días de inactividad)...");

        List<Application> expiredList = applicationRepository.findExpiredApplications(LocalDateTime.now());
        
        if (expiredList.isEmpty()) {
            log.info("No hay solicitudes para expirar hoy.");
            return;
        }

        for (Application app : expiredList) {
            app.setStatus(ApplicationStatus.EXPIRED);
            app.setNotes((app.getNotes() != null ? app.getNotes() + "\n" : "") + 
                "Automáticamente expirada por el sistema debido a inactividad.");
            app.setStatusUpdatedAt(LocalDateTime.now());
        }

        applicationRepository.saveAll(expiredList);
        log.info("{} solicitudes han sido movidas a EXPIRED.", expiredList.size());
    }
}
