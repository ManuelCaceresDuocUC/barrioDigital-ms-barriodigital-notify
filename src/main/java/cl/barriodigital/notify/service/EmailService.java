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
 *
 * <p>QUÉ ES: lógica de negocio del dominio "correo"; el único servicio con un efecto real (envía por SMTP).
 */
@Slf4j
@Service
public class EmailService {

    // Asunto que se usa si el mensaje no trae uno.
    private static final String DEFAULT_SUBJECT = "Notificación BarrioDigital";

    // Cliente de correo de Spring: se configura con el bloque spring.mail de application.yaml.
    private final JavaMailSender mailSender;
    // Interruptor: false = solo simula, true = envía de verdad.
    private final boolean enabled;
    // Remitente del correo (MAIL_FROM o, si no está, MAIL_USERNAME).
    private final String from;

    // @Value lee valores del yaml/variables de entorno; el texto tras ":" es el valor por defecto.
    public EmailService(JavaMailSender mailSender,
                        @Value("${barriodigital.mail.enabled:false}") boolean enabled,
                        @Value("${barriodigital.mail.from:}") String from) {
        // Fail-fast: si se activa el envío real pero falta el remitente, la app no arranca.
        if (enabled && (from == null || from.isBlank())) {
            throw new IllegalStateException(
                    "El envío de correos está activado (MAIL_ENABLED=true) pero falta MAIL_USERNAME o MAIL_FROM");
        }
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void send(EmailNotificationEvent event) {
        // Sin destinatario no hay nada que hacer: error no recuperable (el mensaje va a la DLQ).
        if (event.to() == null || event.to().isBlank()) {
            throw new IllegalArgumentException("El destinatario no puede ser nulo o vacío");
        }
        String to = event.to().trim();
        String subject = isBlank(event.subject()) ? DEFAULT_SUBJECT : event.subject();

        // Modo simulación: se registra con el correo enmascarado y se termina (el mensaje se confirma igual).
        if (!enabled) {
            log.info("[SIMULADO] Correo para {} (trámite {}) con asunto '{}': no se envía (MAIL_ENABLED=false)",
                    maskEmail(to), event.procedureId(), subject);
            return;
        }

        // Modo real: se arma un correo de texto plano.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        // Si no hay cuerpo, se usa el asunto como texto.
        message.setText(isBlank(event.body()) ? subject : event.body());

        // Envío por SMTP. Si falla lanza MailException (RuntimeException): AckHandler reintenta y, si se
        // agotan los intentos, manda el mensaje a la DLQ.
        mailSender.send(message);
        log.info("Correo enviado a {} (trámite {}) con asunto '{}'", maskEmail(to), event.procedureId(), subject);
    }

    /** "vecino@correo.cl" -> "v***@correo.cl": los logs no deben exponer el correo completo. */
    static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "(sin correo)";
        }
        // Posición de la arroba; si no hay o está al inicio, el formato es inválido y se oculta todo.
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        // Primera letra + *** + desde la arroba en adelante.
        return email.charAt(0) + "***" + email.substring(at);
    }

    // true si el texto es null, vacío o solo espacios.
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
