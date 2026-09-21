package cl.barriodigital.notify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.barriodigital.notify.config.RabbitMQConfig;
import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.dto.EmailNotificationEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationListeners {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void handleEmailNotification(EmailNotificationEvent event) {
        log.info("Procesando evento de Email para: {}", event.to());
        if (event.to() == null || event.to().isEmpty()) {
            throw new IllegalArgumentException("El destinatario no puede ser nulo o vacío");
        }
        // Lógica de envío de correo (SMTP / servicio externo)
        log.info("Correo enviado exitosamente a {}", event.to());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CREW)
    public void handleCrewTicket(CrewTicketEvent event) {
        log.info("Procesando ticket de cuadrilla ID: {}", event.ticketId());
        // Lógica de asignación/notificación a cuadrilla
        log.info("Ticket de cuadrilla procesado para la categoría {}", event.category());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CERTIFICATE)
    public void handleCertificateRequest(CertificateEvent event) {
        log.info("Procesando certificado tipo {} para RUT: {}", event.certificateType(), event.citizenRut());
        // Lógica de despacho de certificado
        log.info("Certificado despachado a RUT {}", event.citizenRut());
    }
}