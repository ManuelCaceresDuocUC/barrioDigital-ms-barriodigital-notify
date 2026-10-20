package cl.barriodigital.notify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.service.CertificateService;
import cl.barriodigital.notify.service.CrewTicketService;
import cl.barriodigital.notify.service.EmailService;

@Component
public class NotificationListeners {

    private final EmailService emailService;
    private final CrewTicketService crewTicketService;
    private final CertificateService certificateService;

    public NotificationListeners(EmailService emailService,
                                 CrewTicketService crewTicketService,
                                 CertificateService certificateService) {
        this.emailService = emailService;
        this.crewTicketService = crewTicketService;
        this.certificateService = certificateService;
    }

    @RabbitListener(queues = "${barriodigital.messaging.email.queue}")
    public void handleEmailNotification(EmailNotificationEvent event) {
        emailService.send(event);
    }

    @RabbitListener(queues = "${barriodigital.messaging.crew.queue}")
    public void handleCrewTicket(CrewTicketEvent event) {
        crewTicketService.assign(event);
    }

    @RabbitListener(queues = "${barriodigital.messaging.certificate.queue}")
    public void handleCertificateRequest(CertificateEvent event) {
        certificateService.dispatch(event);
    }
}