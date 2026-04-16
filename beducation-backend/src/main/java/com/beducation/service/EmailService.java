package com.beducation.service;

import com.beducation.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 * Servicio: EmailService
 * ============================================================
 * Gestiona el envío de notificaciones y correos transaccionales
 * usando AWS SES (a través de Spring Mail).
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /** Enviar correo simple a través de AWS SES */
    private void sendSimpleMail(String to, String subject, String text) {
        log.info("Enviando email a {} - Asunto: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Fallo al enviar correo a {}: {}", to, e.getMessage());
            // No bloqueamos el flujo principal si el email falla
        }
    }

    // ──────────────────────────────────────────────
    // EMAILS DE INVITACIÓN / REGISTRO
    // ──────────────────────────────────────────────

    public void sendStudentInvitationEmail(String email, School school) {
        String subject = "Invitación a BeDucation de " + school.getName();
        String text = "Hola,\n\nTu escuela " + school.getName() + " te ha invitado a unirte a BeDucation, " +
            "la plataforma para encontrar prácticas internacionales.\n\n" +
            "Inicia sesión con tu cuenta institucional o personal aquí: " + frontendUrl + "/login\n\n" +
            "El equipo de BeDucation.";
        sendSimpleMail(email, subject, text);
    }

    // ──────────────────────────────────────────────
    // EMAILS DEL FLUJO DE TRABAJO (5 ETAPAS)
    // ──────────────────────────────────────────────

    public void sendApplicationReceivedEmail(Company company, Student student, Opportunity opportunity) {
        String subject = "Nueva solicitud: " + opportunity.getTitle();
        String text = "Hola " + company.getName() + ",\n\n" +
            "El estudiante " + student.getFirstName() + " " + student.getLastName() + 
            " ha enviado una solicitud a tu oferta: " + opportunity.getTitle() + ".\n\n" +
            "Revisa su perfil en el portal: " + frontendUrl + "/company/applications\n\n";
        sendSimpleMail(company.getUser().getEmail(), subject, text);
    }

    public void sendInterestExpressedEmail(Student student, Opportunity opportunity) {
        String subject = "La empresa está interesada en ti - " + opportunity.getTitle();
        String text = "Hola " + student.getFirstName() + ",\n\n" +
            "¡Enhorabuena! " + opportunity.getCompany().getName() + " ha expresado interés en tu solicitud " +
            "para " + opportunity.getTitle() + ".\n\n" +
            "Pronto se pondrán en contacto contigo para programar una entrevista.\n";
        sendSimpleMail(student.getUser().getEmail(), subject, text);
    }

    public void sendInterviewScheduledEmail(Student student, Opportunity opportunity, LocalDateTime dateTime, String link) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String subject = "Entrevista programada - " + opportunity.getCompany().getName();
        String text = "Hola " + student.getFirstName() + ",\n\n" +
            "Se ha programado una entrevista para la oferta " + opportunity.getTitle() + ".\n\n" +
            "Fecha y hora: " + dateTime.format(formatter) + "\n" +
            "Enlace videollamada: " + link + "\n\n" +
            "¡Mucha suerte!\n";
        sendSimpleMail(student.getUser().getEmail(), subject, text);
    }

    public void sendOfferMadeEmail(Student student, Opportunity opportunity) {
        String subject = "¡Has recibido una oferta formal! - " + opportunity.getTitle();
        String text = "Hola " + student.getFirstName() + ",\n\n" +
            opportunity.getCompany().getName() + " te ha hecho una oferta formal para la posición de " + 
            opportunity.getTitle() + ".\n\n" +
            "Entra al portal para aceptarla o rechazarla: " + frontendUrl + "/student/applications\n";
        sendSimpleMail(student.getUser().getEmail(), subject, text);
    }

    public void sendOfferRejectedEmail(Company company, Application application) {
        String subject = "El candidato ha declinado la oferta - " + application.getOpportunity().getTitle();
        String text = "El candidato " + application.getStudent().getFirstName() + 
            " ha decidido rechazar la oferta para " + application.getOpportunity().getTitle() + ".\n";
        sendSimpleMail(company.getUser().getEmail(), subject, text);
    }

    public void sendPlacementPendingAdminEmail(Application application) {
        // En un entorno real se buscaría a los usuarios ADMIN, aquí enviamos un correo de soporte genérico
        log.info("ADMIN EMAIL: Pendiente revisión de placement (Etapa 4) para app_id: {}", application.getId());
    }

    public void sendFinalApprovalRequestEmail(School school, Application application) {
        String subject = "Acción Requerida: Aprobación final de placement";
        String text = "Hola " + school.getName() + ",\n\n" +
            "El alumno " + application.getStudent().getFirstName() + " ha aceptado una oferta que ha sido pre-aprobada.\n" +
            "Accede a tu panel para dar la confirmación final y gestionar el convenio.\n\n" +
            frontendUrl + "/school/placements\n";
        sendSimpleMail(school.getUser().getEmail(), subject, text);
    }

    public void sendPlacementConfirmedEmail(Application application) {
        String subject = "¡Prácticas Confirmadas! - " + application.getOpportunity().getTitle();
        String text = "El placement ha sido confirmado por la escuela. El proceso para la oferta ha concluido con éxito.";
        
        // Notificar al estudiante
        sendSimpleMail(application.getStudent().getUser().getEmail(), subject, text);
        // Notificar a la empresa
        sendSimpleMail(application.getOpportunity().getCompany().getUser().getEmail(), subject, text);
    }

    public void sendPlacementRejectedEmail(Application application, String reason) {
        String subject = "Prácticas No Aprobadas por tu Escuela";
        String text = "La escuela no ha aprobado el placement.\nMotivo: " + reason;
        sendSimpleMail(application.getStudent().getUser().getEmail(), subject, text);
    }

    public void sendRejectionEmail(Student student, Opportunity opportunity, String reason) {
        String subject = "Actualización sobre tu solicitud - " + opportunity.getTitle();
        String text = "Hola " + student.getFirstName() + ",\n\n" +
            "Lamentamos informarte de que la empresa ha decidido no continuar con tu candidatura para " +
            opportunity.getTitle() + ".\n\nOjalá tengas suerte en otras ofertas activas.";
        sendSimpleMail(student.getUser().getEmail(), subject, text);
    }

    // ──────────────────────────────────────────────
    // EMAILS DE GALERÍA DE FOTOS
    // ──────────────────────────────────────────────

    public void sendGalleryAccessApprovedEmail(User user) {
        String subject = "Acceso concedido a la Galería de Fotos - BeDucation";
        String text = "¡Hola!\n\nTu solicitud de acceso a la galería de fotos ha sido aprobada.\n" +
            "Ya puedes ver todas las fotos del evento desde tu panel principal.\n\n" +
            "Disfrútalas,\nEl equipo de BeDucation.";
        sendSimpleMail(user.getEmail(), subject, text);
    }

    public void sendGalleryAccessRejectedEmail(User user, String reason) {
        String subject = "Actualización sobre tu acceso a la Galería";
        String text = "Hola,\n\nLamentamos informarte que tu solicitud de acceso a la galería ha sido denegada.\n" +
            (reason != null && !reason.isEmpty() ? "Motivo: " + reason + "\n\n" : "") +
            "Si crees que esto es un error, contacta con soporte.\n\n" +
            "Saludos.";
        sendSimpleMail(user.getEmail(), subject, text);
    }
}
