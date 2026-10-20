package cl.barriodigital.notify.service;

import org.springframework.stereotype.Service;

import cl.barriodigital.notify.dto.EmailNotificationEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    public void send(EmailNotificationEvent event) {
        if (event.to() == null || event.to().isBlank()) {
            throw new IllegalArgumentException("El destinatario no puede ser nulo o vacío");
        }
        log.info("Procesando correo para {} (trámite {})", event.to(), event.procedureId());
        // Simulación del envío; en producción iría SMTP o un servicio externo.
        log.info("Correo enviado a {} con asunto '{}'", event.to(), event.subject());
    }
}