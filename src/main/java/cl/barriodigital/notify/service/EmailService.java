package cl.barriodigital.notify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import cl.barriodigital.notify.dto.EmailNotificationEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Envía los correos del dominio de notificaciones.
 *
 * <p>Con {@code barriodigital.mail.enabled=false} (por defecto) solo registra que lo simula: sirve para
 * desarrollo y pruebas sin servidor SMTP. Con {@code true} envía por SMTP. Un fallo de envío
 * ({@code MailException}) se propaga: lo resuelve el manejo de ACK/NACK del consumidor (reintentos y DLQ),
 * de modo que un correo que no salió nunca se da por entregado.
 */
@Slf4j
@Service
public class EmailService {

    private static final String DEFAULT_SUBJECT = "Notificación BarrioDigital";

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${barriodigital.mail.enabled:false}") boolean enabled,
                        @Value("${barriodigital.mail.from:}") String from) {
        if (enabled && (from == null || from.isBlank())) {
            throw new IllegalStateException(
                    "El envío de correos está activado (MAIL_ENABLED=true) pero falta MAIL_USERNAME o MAIL_FROM");
        }
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void send(EmailNotificationEvent event) {
        if (event.to() == null || event.to().isBlank()) {
            throw new IllegalArgumentException("El destinatario no puede ser nulo o vacío");
        }
        String to = event.to().trim();
        String subject = isBlank(event.subject()) ? DEFAULT_SUBJECT : event.subject();

        if (!enabled) {
            log.info("[SIMULADO] Correo para {} (trámite {}) con asunto '{}': no se envía (MAIL_ENABLED=false)",
                    maskEmail(to), event.procedureId(), subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(isBlank(event.body()) ? subject : event.body());

        mailSender.send(message);
        log.info("Correo enviado a {} (trámite {}) con asunto '{}'", maskEmail(to), event.procedureId(), subject);
    }

    /** "vecino@correo.cl" -> "v***@correo.cl": los logs no deben exponer el correo completo. */
    static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "(sin correo)";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
